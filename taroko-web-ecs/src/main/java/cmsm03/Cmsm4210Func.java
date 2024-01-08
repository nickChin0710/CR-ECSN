package cmsm03;
/** cmsm4210 (卡友權益資格參數維護)
 * 108-10-31   JH                modify
 * 109-01-09   JustinWu   add six new columns and new selection of card_hldr_flag
 * 109-04-20  shiyuqi       updated for project coding standard     *
 * 109-10-07  JustinWu     add new columns
 * 111-12-30  ZuweiSu      版面調整,新增/修改存檔,檢核調整
 * 112-03-07  Machao       版面調整,當年消費門檻
 * 112-03-14  Machao       版面調整,優惠價
 * 112-03-24  machao       cmsm4210 測試結果需再調整
 * 112-05-29  ZuweiSu      item_no='15'需能勾選及輸入優惠價
 * 112-06-09  machao       版面調整，
 * 112-07-19  machao       數據庫新增choose_cond欄位，增添相關邏輯
* */
import busi.FuncAction;

public class Cmsm4210Func extends FuncAction {
  String itemNo = "", projCode = "";

  @Override
  public void dataCheck() {
    if (ibAdd) {
      itemNo = wp.itemStr2("kk_item_no");
      projCode = wp.itemStr2("kk_proj_code");
    } else {
      itemNo = wp.itemStr2("item_no");
      projCode = wp.itemStr2("proj_code");
    }

    if (empty(itemNo)) {
      errmsg("權益代碼 不可空白");
      return;
    }
    if (empty(projCode)) {
      errmsg("專案代碼 不可空白");
      return;
    }

    if (ibDelete)
      return;

    if (wp.itemEmpty("proj_desc")) {
      errmsg("專案別說明:不可空白");
      return;
    }

    // --首年認定
    if (wp.itemEq("debut_year_flag", "1") || wp.itemEq("debut_year_flag", "2")) {
      if (wp.itemEmpty("debut_sup_flag_0") && wp.itemEmpty("debut_sup_flag_1")) {
        errmsg("首年認定：正附卡不可皆為空白");
        return;
      }
    }
    if (wp.itemEq("debut_year_flag", "1") && wp.itemEmpty("debut_month1")) {
      errmsg("首年認定 新發卡：核卡前幾個月不可空白");
      return;
    }
    if (wp.itemEq("debut_year_flag", "2") && wp.itemEmpty("debut_month2")) {
      errmsg("首年認定 首辦卡：前一年度第幾個月不可空白");
      return;
    }

    // --消費金額本金類計算 目前為有勾選項目才計算，若全部空白則皆不納入計算 2019/07/25
    String lsConsume = "";
    lsConsume = wp.itemNvl("consume_bl", "N") + wp.itemNvl("consume_ca", "N")
        + wp.itemNvl("consume_it", "N") + wp.itemNvl("consume_ao", "N")
        + wp.itemNvl("consume_id", "N") + wp.itemNvl("consume_ot", "N");

    if (wp.itemNe("consume_type", "0") && eqIgno(lsConsume, "NNNNNN")) {
      errmsg("消費金額本金類：不可皆為空白");
      return;
    }

    // --當年消費門檻
    if (wp.itemEq("curr_cond", "Y")) {
    	if(empty(wp.itemStr("choose_cond"))) {
    		errmsg("指定刷卡期間必選其一");
    		return;
    	}else {
    		if(wp.itemEq("choose_cond", "1")) {
    			if(wp.itemNum("curr_pre_month")==0 ) {
    				errmsg("請輸入上一年度月份");
    	    		return;	
    			}
    			 if (wp.itemNum("curr_min_amt") <= 0) {
 			        errmsg("指定刷卡期間,單筆最低消費金額需>0");
 			        return;
 			      }
    		}
    		if(wp.itemEq("choose_cond", "2")){
    			if(wp.itemNum("last_mm")==0 || wp.itemNum("curr_min_amt")==0) {
    				errmsg("近N個月刷卡消費,單筆最低消費金額必須輸入");
    	    		return;		
    			}else {
    				if (wp.itemNum("last_mm") <= 0) {
    			        errmsg("近N個月刷卡消費要大於0 ");
    			        return;
    			      }
    			      if (wp.itemNum("curr_min_amt") <= 0) {
    			        errmsg("單筆最低消費金額要大於0 ");
    			        return;
    			      }
    			}
    		}
    	}
    	if(wp.itemEq("curr_amt_cond", "Y")) {
			if (wp.itemNum("curr_amt") == 0) {
		        errmsg("累積刷卡金額達必須輸入 ");
		        return;
		      }
			if (wp.itemNum("curr_amt") <= 0) {
		        errmsg("累積刷卡金額達要大於0 ");
		        return;
		      }
		}
    	if(wp.itemEq("curr_cnt_cond", "Y")){
//			if (wp.itemNum("curr_tot_cnt") == 0 || wp.itemNum("curr_cnt") == 0) {
//		        errmsg("累積消費筆數達或享有次數必須輸入");
//		        return;
//		      }
			if (wp.itemNum("curr_cnt") <= 0) {
		        errmsg("享有次數要大於0 ");
		        return;
		      }
			if (wp.itemNum("curr_tot_cnt") <= 0) {
		        errmsg("累積消費筆數達筆數要大於0 ");
		        return;
		      }
		}
    	if(wp.itemEq("choose_cond", "1")) {
    		wp.itemSet("last_mm", "0");
//            wp.itemSet("curr_min_amt", "0");
    	}
    	if(wp.itemEq("choose_cond", "2")){
    		wp.itemSet("curr_pre_month", "0");
    	}
    	
    } 
    
    if(!wp.itemEq("curr_cond", "Y") && !wp.itemEq("air_cond", "Y")) {
    	errmsg("當年消費門檻必勾選,　06.團體代號+MCC CODE 其中一項需勾選(2選1) ");
        return;
    }
    
    if ((this.ibAdd) || (this.ibUpdate)) {
    	if(wp.itemEq("consume_type", "0")) {
    		if(wp.itemNum("consume_00_cnt")<=0) {
    			errmsg("不計算消費金額可使用次數>0 ");
    			wp.colSet("consume_00_cnt_pink", "pink");
    	        return;
    		}
    	}
    	if (wp.itemEq("curr_cond", "Y")) {
    		if(wp.itemNum("curr_cnt")<=0) {
    			errmsg("享有次數>0 ");
    			wp.colSet("curr_cnt_pink", "pink");
    	        return;
    		}
    	}
    }
    
    // --前一年度消費門檻
    if (wp.itemEq("last_cond", "Y")) {
      if (chkAmtCnt() == false)
        return;
    }

    // --刷卡金額每增加
    if (wp.itemEq("cond_per", "Y")) {
      if (wp.itemNum("per_amt") <= 0) {
        errmsg("刷卡金額每增加金額不可小於等於 0 ");
        return;
      }

      if (wp.itemNum("per_cnt") <= 0) {
        errmsg("刷卡金額每增加享有權益不可小於等於 0 ");
        return;
      }
    }

    // --核卡後X日刷團費或機票
    if (wp.itemEq("air_cond", "Y")) {
      if (wp.itemEmpty("air_sup_flag_0") && wp.itemEmpty("air_sup_flag_1")) {
        errmsg("核卡後刷團費或機票：正附卡 不可皆為空白");
        return;
      }

      if (wp.itemEmpty("air_day")) {
        errmsg("核卡後刷團費或機票：幾日不可空白 ");
        return;
      }

      if (wp.itemEmpty("air_amt_type")) {
        errmsg("核卡後刷團費或機票：金額計算方式不可空白");
        return;
      }

      if (wp.itemNum("air_amt") <= 0) {
        errmsg("核卡後刷團費或機票：消費金額不可小於等於 0 ");
        return;
      }

      if (wp.itemNum("air_cnt") <= 0) {
        errmsg("核卡後刷團費或機票：享有權益次數不可小於等於 0 ");
        return;
      }

    }
    
    // --優惠價
    if(wp.itemEq("price_cond", "Y")) {
    	if (wp.itemNum("price") <= 0) {
            errmsg("優惠價不可小於等於 0 ");
            return;
          }
    	if(!itemNo.equals("10") && !itemNo.equals("11") && !itemNo.equals("15")) {
    		errmsg("非貴賓室,不可選取優惠價 ");
            return;
    	}
    }else {
    	if(wp.itemNum("price") > 0) {
    		errmsg("優惠價不可輸入");
            return;
    	}
    }

//    // --請款比對使用條件 A
//    if (wp.itemEq("a_use_cond", "Y")) {
//      if (wp.itemEmpty("a_last_month")) {
//        errmsg("請款比對使用條件 A：近幾個月刷團費或機票不可空白");
//        return;
//      }
//
//      if (wp.itemEmpty("a_use_amt_type")) {
//        errmsg("請款比對使用條件 A：金額計算方式不可空白");
//        return;
//      }
//
//      if (wp.itemNum("a_use_amt") <= 0) {
//        errmsg("請款比對使用條件 A：金額不可小於等於 0 ");
//        return;
//      }
//    }
//
//    // --請款比對使用條件 B
//    if (wp.itemEq("b_use_cond", "Y")) {
//      if (wp.itemEmpty("b_last_month")) {
//        errmsg("請款比對使用條件 B：近幾個月累積消費不可空白");
//        return;
//      }
//
//      if (wp.itemNum("b_use_amt") <= 0) {
//        errmsg("請款比對使用條件 B：累積消費金額不可小於等於 0 ");
//        return;
//      }
//
//      if (wp.itemEmpty("b_use_amt_type")) {
//        errmsg("請款比對使用條件 B：消費金額累積方式不可空白 ");
//        return;
//      }
//
//      String lsUseType = "";
//      lsUseType = wp.itemNvl("b_use_bl", "N") + wp.itemNvl("b_use_ca", "N")
//          + wp.itemNvl("b_use_it", "N") + wp.itemNvl("b_use_ao", "N") + wp.itemNvl("b_use_id", "N")
//          + wp.itemNvl("b_use_ot", "N");
//
//      if (eqIgno(lsUseType, "NNNNNN")) {
//        errmsg("請款比對使用條件 B：消費金額本金類不可空白 ");
//        return;
//      }
//    }
//
//    // --請款比對使用條件 D
//    if (wp.itemEq("d_use_cond", "Y")) {
//      if (wp.itemEmpty("d_use_right")) {
//        errmsg("當年度權益需二選一");
//        return;
//      }
//    }

    if (ibAdd)
      return;
    
    if ((this.ibAdd) || (this.ibUpdate)) {
    	if(!wp.itemEq("acct_type_flag", "Y")) {
    		errmsg("01.帳戶類別必勾選,存檔後,需新增明細資料");
            return;
    	}
    }
    

    // --因主檔新增後才可新增明細故在修改時檢核明細相關條件

    // if(wp.item_eq("acct_type_flag","Y")){
    // if(checkDetl("01")==false){
    // errmsg("明細：01.帳戶類別 不可全部空白 ");
    // return ;
    // }
    // }

    // if(wp.item_eq("group_card_flag","Y")){
    // if(checkDetl("02")==false){
    // errmsg("明細：02.指定團代+卡種 不可全部空白 ");
    // return ;
    // }
    // }

    // if(wp.item_eq("debut_year_flag", "1")){
    // if(checkDetl("03")==false){
    // errmsg("明細：03.團體代號 不可全部空白 ");
    // return ;
    // }
    // }

    // if(wp.item_eq("debut_year_flag", "2")){
    // if(checkDetl("04")==false){
    // errmsg("明細：04.團體代號 不可全部空白 ");
    // return ;
    // }
    // }

    // if(wp.item_eq("cond_per", "Y")){
    // if(checkDetl("05")==false){
    // errmsg("明細：05.團體代號 不可全部空白 ");
    // return ;
    // }
    // }

    // if(wp.item_eq("air_cond", "Y")){
    // if(checkDetl("06")==false){
    // errmsg("明細：06.團體代號 不可全部空白 ");
    // return ;
    // }
    //
    // if(checkDetl("07")==false){
    // errmsg("明細：07.MCC CODE 不可全部空白 ");
    // return ;
    // }
    // }

    // if(wp.item_eq("a_use_cond", "Y") && wp.item_eq("a_mcc_code", "Y")){
    // if(checkDetl("08")==false){
    // errmsg("明細：08.MCC CODE 不可全部空白 ");
    // return ;
    // }
    // }

    // if(wp.item_eq("d_use_cond", "Y")){
    // if(checkDetl("09")==false){
    // errmsg("明細：09.MCC CODE 不可全部空白 ");
    // return ;
    // }
    // }

  }

  boolean chkAmtCnt() {

    int rr = 1;

    double lmAmt = 0;

    for (int ii = 6; ii > 0; ii--) {

      // if (wp.item_num("last_amt"+ii)==0 && wp.item_num("last_cnt"+ii)==0) continue;
      if (lmAmt == 0 && wp.itemNum("last_amt" + ii) == 0)
        continue;
      
      if (wp.itemNum("last_amt" + ii) <= 0) {
        errmsg("前一年消費門檻 (" + ii + ") 金額 不可為0 !");
        return false;
      }
      if (wp.itemNum("last_cnt" + ii) == 0) {
        errmsg("前一年消費門檻 (" + ii + ") 次數需 >0 !");
        return false;
      }

      // if ( (wp.item_num("last_amt"+ii)!=0 && wp.item_num("last_cnt"+ii)==0) ||
      // (wp.item_num("last_amt"+ii)==0 && wp.item_num("last_cnt"+ii)!=0) ) {
      // errmsg("前一年消費門檻 ("+ii+") 金額/次數需同時 =0 or >0 !");
      // return false;
      // }

      if (lmAmt > 0 && wp.itemNum("last_amt" + ii) >= lmAmt) {
        errmsg("前一年消費門檻 (" + ii + ") 金額, 輸入錯誤");
        return false;
      }

      // --金額若有輸入 次數需大於 0 , 次數若有輸入 金額需大於 0
      if (wp.itemNum("last_amt" + ii) > 0) {
        if (wp.itemNum("last_cnt" + ii) <= 0) {
          errmsg("前一年消費門檻 (" + ii + ") 次數需大於 0 !");
          return false;
        }
      }
      if (wp.itemNum("last_cnt" + ii) > 0) {
        if (wp.itemNum("last_amt" + ii) <= 0) {
          errmsg("前一年消費門檻 (" + ii + ") 金額需大於 0 !");
          return false;
        }
      }

      lmAmt = wp.itemNum("last_amt" + ii);
      // if(ii==1) continue;
      // --確認金額、次數區間
      //
      // if(wp.item_num("last_amt"+ii) <= wp.item_num("last_amt"+rr)){
      // errmsg("前一年消費門檻 ("+rr+") 、 ("+ii+") 金額區間輸入錯誤");
      // return false;
      // }
      //
      // if(wp.item_num("last_cnt"+ii) <= wp.item_num("last_cnt"+rr)){
      // errmsg("前一年消費門檻 ("+rr+") 、 ("+ii+") 次數區間輸入錯誤");
      // return false;
      // }
      // rr++;
    }

    if (lmAmt == 0)
      return false;

    return true;
  }

  boolean checkDetl() {
    String lsType = wp.itemStr2("data_type");
    String lsCode = wp.itemStr2("ex_data_code");
    String lsCode2 = wp.itemStr2("ex_data_code2");
//    String lsCode3 = wp.itemStr2("ex_data_code3");
    
    if (eq(lsType, "01")) {
      if (empty(lsCode)) {
        errmsg("帳戶類別: 不可空白");
        return false;
      }
    } else if (eq(lsType, "02")) {
      if (empty(lsCode) && empty(lsCode2)) {
        errmsg("團代、卡種: 不可皆為空白");
        return false;
      }
    } else if (eq(lsType, "03")) {
      if (empty(lsCode)) {
        errmsg("團代: 不可空白");
        return false;
      }
    } else if (eq(lsType, "05")) {
      if (empty(lsCode)) {
        errmsg("團代: 不可空白");
        return false;
      }
    } else if (eq(lsType, "06") || eq(lsType, "07") || eq(lsType, "08")) {
      if (empty(lsCode)) {
        errmsg("團代: 不可空白");
        return false;
      }
    } else {
      errmsg("資料類別須為(01,02,03,04,05,06,07,08)");
      return false;
    }

    // -check group_code-
    String sql1 = "";
    if (pos(",02,03,04,05,06,07,08", lsType) > 0) {
      sql1 = "select count(*) as xx_cnt from ptr_group_code" + " where group_code =?";
      double llCnt = getNumber(sql1, lsCode);
      if (llCnt <= 0) {
        errmsg("團代: 輸入錯誤");
        return false;
      }
    }
    
    if (eq(lsType, "02") ) {
      sql1 = "select count(*) from ptr_card_type" + " where card_type =?";
      double llCnt2 = getNumber(sql1, lsCode2);
      if (llCnt2 <= 0) {
        errmsg("卡種: 輸入錯誤");
        return false;
      }
    }

    return true;
  }

  @Override
  public int dbInsert() {
    actionInit("A");

    dataCheck();
    if (rc != 1)
      return rc;

    insertRightParm();

    return rc;
  }

  int insertRightParm() {

    sql2Insert("cms_right_parm");
    addsqlParm(" ?", " item_no", itemNo);
    addsqlParm(",?", ", proj_code", projCode);
    addsqlParm(", apr_flag", ",'N'");
    addsqlParm(",?", ", active_status", wp.itemStr2("active_status"));
    addsqlParm(",?", ", proj_desc", wp.itemStr2("proj_desc"));
    addsqlParm(",?", ", acct_type_flag", wp.itemYn("acct_type_flag"));
    addsqlParm(",?", ", card_hldr_flag  ", wp.itemStr2("card_hldr_flag"));
    addsqlParm(",?", ", group_card_flag ", wp.itemYn("group_card_flag"));
    addsqlParm(",?", ", debut_year_flag ", wp.itemStr2("debut_year_flag "));
    addsqlParm(",?", ", debut_month1    ", wp.itemNum("debut_month1"));
    addsqlParm(",?", ", debut_sup_flag_0", wp.itemYn("debut_sup_flag_0"));
    addsqlParm(",?", ", debut_sup_flag_1", wp.itemYn("debut_sup_flag_1"));
    addsqlParm(",?", ", debut_month2 ", wp.itemNum("debut_month2"));
    addsqlParm(",?", ", debut_group_cond", wp.itemNvl("debut_group_cond", "0"));
    addsqlParm(",?", ", consume_type", wp.itemStr2("consume_type"));
    addsqlParm(",?", ", consume_00_cnt  ", wp.itemNum("consume_00_cnt  "));
    addsqlParm(",?", ", consume_bl", wp.itemYn("consume_bl"));
    addsqlParm(",?", ", consume_ca", wp.itemYn("consume_ca"));
    addsqlParm(",?", ", consume_it", wp.itemYn("consume_it"));
    if (!wp.itemEmpty("consume_it")) {
      addsqlParm(",?", ", it_1_type", wp.itemStr("it_1_type"));
    } else {
      addsqlParm(",?", ", it_1_type", "");
    }
    addsqlParm(",?", ", consume_ao", wp.itemYn("consume_ao"));
    addsqlParm(",?", ", consume_id", wp.itemYn("consume_id"));
    addsqlParm(",?", ", consume_ot", wp.itemYn("consume_ot"));
    addsqlParm(",?", ", curr_cond", wp.itemYn("curr_cond"));
    addsqlParm(",?", ", choose_cond", wp.itemYn("choose_cond"));
    addsqlParm(",?", ", curr_pre_month  ", wp.itemNum("curr_pre_month"));
    addsqlParm(",?", ", last_mm", wp.itemNum("last_mm"));
//    addsqlParm(",?", ", curr_min_amt", wp.itemNum("curr_min_amt"));
    addsqlParm(",?", ", curr_amt", wp.itemNum("curr_amt"));
//    addsqlParm(",?", ", curr_tot_cnt", wp.itemNum("curr_tot_cnt"));
    addsqlParm(",?", ", curr_cnt", wp.itemNum("curr_cnt"));
    addsqlParm(",?", ", last_cond", wp.itemYn("last_cond"));
    addsqlParm(",?", ", last_amt1", wp.itemNum("last_amt1"));
    addsqlParm(",?", ", last_cnt1", wp.itemNum("last_cnt1"));
    addsqlParm(",?", ", last_amt2", wp.itemNum("last_amt2"));
    addsqlParm(",?", ", last_cnt2", wp.itemNum("last_cnt2"));
    addsqlParm(",?", ", last_amt3", wp.itemNum("last_amt3"));
    addsqlParm(",?", ", last_cnt3", wp.itemNum("last_cnt3"));
    addsqlParm(",?", ", last_amt4", wp.itemNum("last_amt4"));
    addsqlParm(",?", ", last_cnt4", wp.itemNum("last_cnt4"));
    addsqlParm(",?", ", last_amt5", wp.itemNum("last_amt5"));
    addsqlParm(",?", ", last_cnt5", wp.itemNum("last_cnt5"));
    addsqlParm(",?", ", last_amt6", wp.itemNum("last_amt6"));
    addsqlParm(",?", ", last_cnt6", wp.itemNum("last_cnt6"));
    addsqlParm(",?", ", cond_per", wp.itemYn("cond_per"));
    addsqlParm(",?", ", per_amt", wp.itemNum("per_amt"));
    addsqlParm(",?", ", per_cnt", wp.itemNum("per_cnt"));
    addsqlParm(",?", ", air_cond", wp.itemYn("air_cond"));
    addsqlParm(",?", ", air_sup_flag_0  ", wp.itemYn("air_sup_flag_0"));
    addsqlParm(",?", ", air_sup_flag_1  ", wp.itemYn("air_sup_flag_1"));
    addsqlParm(",?", ", air_day", wp.itemNum("air_day"));
    addsqlParm(",?", ", air_amt_type", wp.itemStr2("air_amt_type"));
    addsqlParm(",?", ", air_amt", wp.itemNum("air_amt"));
    addsqlParm(",?", ", air_cnt", wp.itemNum("air_cnt"));
    addsqlParm(",?", ", price_cond", wp.itemYn("price_cond"));
    addsqlParm(",?", ", price", wp.itemNum("price"));
    addsqlParm(",?", ", air_mcc_group07 ", wp.itemStr("air_mcc_group07"));
//    addsqlParm(",?", ", a_use_cond", wp.itemYn("a_use_cond"));
//    addsqlParm(",?", ", a_last_month", wp.itemNum("a_last_month"));
//    addsqlParm(",?", ", a_use_amt_type", wp.itemStr("a_use_amt_type"));
//    addsqlParm(",?", ", a_use_amt", wp.itemNum("a_use_amt"));
//    addsqlParm(",?", ", a_use_type", wp.itemStr("a_use_type"));
//    addsqlParm(",?", ", a_air_right", wp.itemYn("a_air_right"));
//    addsqlParm(",?", ", a_mcc_group", wp.itemYn("a_mcc_group"));
//    addsqlParm(",?", ", b_use_cond", wp.itemYn("b_use_cond"));
//    addsqlParm(",?", ", b_last_month", wp.itemNum("b_last_month"));
//    addsqlParm(",?", ", b_use_amt", wp.itemNum("b_use_amt"));
//    addsqlParm(",?", ", b_use_amt_type  ", wp.itemStr("b_use_amt_type"));
//    addsqlParm(",?", ", b_use_bl", wp.itemYn("b_use_bl"));
//    addsqlParm(",?", ", b_use_ca", wp.itemYn("b_use_ca"));
//    addsqlParm(",?", ", b_use_it", wp.itemYn("b_use_it"));
//    if (!wp.itemEmpty("b_use_it")) {
//      addsqlParm(",?", ", it_2_type", wp.itemStr("it_2_type"));
//    } else {
//      addsqlParm(",?", ", it_2_type", "");
//    }
//    addsqlParm(",?", ", b_use_ao", wp.itemYn("b_use_ao"));
//    addsqlParm(",?", ", b_use_id", wp.itemYn("b_use_id"));
//    addsqlParm(",?", ", b_use_ot", wp.itemYn("b_use_ot"));
//    addsqlParm(",?", ", c_use_cond", wp.itemYn("c_use_cond"));
//    addsqlParm(",?", ", d_use_cond", wp.itemYn("d_use_cond"));
//    addsqlParm(",?", ", d_use_right", wp.itemStr("d_use_right"));
//    addsqlParm(",?", ", d_mcc_group", wp.itemStr("d_mcc_group"));
//    addsqlParm(",?", ", e_use_cond", wp.itemYn("e_use_cond"));
    addsqlParm(",?", ", proj_date_s", wp.itemStr("proj_date_s"));
    addsqlParm(",?", ", proj_date_e", wp.itemStr("proj_date_e"));
    addsqlParm(",?", ", item_month_s", wp.itemStr("item_month_s"));
    addsqlParm(",?", ", item_month_e", wp.itemStr("item_month_e"));
    // 2020-09-26 Justin
    addsqlParm(",?", ", curr_amt_cond", wp.itemYn("curr_amt_cond"));
    addsqlParm(",?", ", curr_cnt_cond", wp.itemYn("curr_cnt_cond"));
    addsqlParm(",?", ", use_cnt_cond", wp.itemStr("use_cnt_cond"));
    addsqlParm(",?", ", use_max_cnt", wp.itemNum("use_max_cnt"));
    addsqlParm(",?", ", curr_min_amt", wp.itemNum("curr_min_amt"));
    addsqlParm(",?", ", curr_tot_cnt", wp.itemNum("curr_tot_cnt"));
    // 2020-09-26 Justin
    
    if (wp.itemEmpty("rowid")) {
      addsqlParm(", crt_date", "," + commSqlStr.sysYYmd);
      addsqlParm(",?", ", crt_user        ", modUser);
    } else {
      addsqlParm(",?", ", crt_date", wp.itemStr2("crt_date"));
      addsqlParm(",?", ", crt_user", wp.itemStr2("crt_user"));
    }
    addsqlModXXX(modUser, modPgm);
    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      errmsg("insert cms_right_parm error ");
    }

    return rc;
  }

  public int insertDetl() {
    msgOK();
    if (checkDetl() == false) {
      return rc;
    }

    strSql = "delete cms_right_parm_detl where table_id='RIGHT' and proj_code=? and item_no=?"
        + " and data_type =? and apr_flag=? and data_code=? and data_code2=? and data_code3=? ";
    setString2(1, wp.itemStr2("proj_code"));
    setString(wp.itemStr2("item_no"));
    setString(wp.itemStr2("data_type"));
    setString(wp.itemStr2("apr_flag"));
    setString(wp.itemStr2("ex_data_code"));
    setString(wp.itemStr2("ex_data_code2"));
    setString(wp.itemStr2("ex_data_code3"));
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      sqlErr("update cms_right_parm_detl error");
      return -1;
    }else if(sqlRowNum > 0){
      errmsg("此筆資料已存在");
      return -1;
    }

    sql2Insert("cms_right_parm_detl");
    addsqlParm(" ?", " table_id", "RIGHT");
    addsqlParm(",?", ", proj_code", wp.itemStr2("proj_code"));
    addsqlParm(",?", ", item_no", wp.itemStr2("item_no"));
    addsqlParm(", apr_flag", ",'N'");
    addsqlParm(",?", ", data_type", wp.itemStr2("data_type"));
    addsqlParm(",?", ", data_code", wp.itemStr2("ex_data_code"));
    addsqlParm(",?", ", data_code2", wp.itemStr2("ex_data_code2"));
    addsqlParm(",?", ", data_code3", wp.itemStr2("ex_data_code3"));
    addsqlParm(", mod_time", ", sysdate");
    addsqlParm(",?", ", mod_pgm", modPgm);
    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      errmsg("insert cms_right_parm_detl error !");
    }

    // if (rc==1) {
    // update_Right_parm_cond();
    // }

    return rc;
  }

  // void update_Right_parm_cond() {
  // String ls_proj_code =wp.col_ss("proj_code");
  // String ls_item_no =wp.col_ss("item_no");
  // String ls_data_type =wp.col_ss("data_type");
  //
  // String sql1 ="select count(*) as xx_cnt"+
  // " from cms_right_parm_detl "+
  // " where table_id='RIGHT' and apr_flag<>'Y'"+
  // " and proj_code =? and item_no =? and data_type =?";
  // ppp(1,ls_proj_code);
  // ppp(ls_item_no);
  // ppp(ls_data_type);
  // sqlSelect(sql1);
  // }

  public int deleteDetl(int ll) {
    msgOK();

    String lsCode = wp.colStr(ll, "data_code");
    String lsCode2 = wp.colStr(ll, "data_code2");
    String lsCode3 = wp.colStr(ll, "data_code3");
    
    strSql = "delete cms_right_parm_detl where table_id='RIGHT' and proj_code=? and item_no=?"
        + " and data_type =? and apr_flag=? and data_code=? and data_code2=? and data_code3=? ";
    setString2(1, wp.itemStr2("proj_code"));
    setString(wp.itemStr2("item_no"));
    setString(wp.itemStr2("data_type"));
    setString(wp.itemStr2("apr_flag"));
    setString(lsCode);
    setString(lsCode2);
    setString(lsCode3);

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      sqlErr("update cms_right_parm_detl");
    }

    return rc;
  }

  public int deleteAllDetl() {
    msgOK();

    strSql = "delete cms_right_parm_detl where table_id ='RIGHT' "
        + " and  proj_code =? and item_no =? and apr_flag='N'";

    setString2(1, wp.itemStr2("proj_code"));
    setString(wp.itemStr2("item_no"));

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete cms_right_parm_detl error !");
      return rc;
    } else
      rc = 1;

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;
    if (wp.itemEq("apr_flag", "Y")) {
      errmsg("已覆核不可修改");
      return rc;
    }

    sql2Update("cms_right_parm");
    addsqlParm("  active_status   =?", wp.itemStr("active_status"));
    addsqlParm(", proj_desc       =?", wp.itemStr("proj_desc"));
    addsqlParm(", acct_type_flag  =?", wp.itemYn("acct_type_flag"));
    addsqlParm(", card_hldr_flag  =?", wp.itemStr("card_hldr_flag  "));
    addsqlParm(", group_card_flag =?", wp.itemYn("group_card_flag "));
    addsqlParm(", debut_year_flag =?", wp.itemStr("debut_year_flag "));
    addsqlParm(", debut_month1    =?", wp.itemNum("debut_month1    "));
    addsqlParm(", debut_sup_flag_0=?", wp.itemYn("debut_sup_flag_0"));
    addsqlParm(", debut_sup_flag_1=?", wp.itemYn("debut_sup_flag_1"));
    addsqlParm(", debut_month2    =?", wp.itemNum("debut_month2    "));
    addsqlParm(", debut_group_cond=?", wp.itemNvl("debut_group_cond", "0"));
    addsqlParm(", consume_type    =?", wp.itemStr("consume_type    "));
    addsqlParm(", consume_00_cnt  =?", wp.itemNum("consume_00_cnt  "));
    addsqlParm(", consume_bl      =?", wp.itemYn("consume_bl      "));
    addsqlParm(", consume_ca      =?", wp.itemYn("consume_ca      "));
    addsqlParm(", consume_it      =?", wp.itemYn("consume_it      "));
    if (!wp.itemEmpty("consume_it")) {
      addsqlParm(", it_1_type      =?", wp.itemStr("it_1_type"));
    } else {
      addsqlParm(", it_1_type      =?", "");
    }
    addsqlParm(", consume_ao      =?", wp.itemYn("consume_ao      "));
    addsqlParm(", consume_id      =?", wp.itemYn("consume_id      "));
    addsqlParm(", consume_ot      =?", wp.itemYn("consume_ot      "));
    addsqlParm(", curr_cond       =?", wp.itemYn("curr_cond       "));
    addsqlParm(", choose_cond     =?", wp.itemYn("choose_cond     "));
    addsqlParm(", curr_pre_month  =?", wp.itemNum("curr_pre_month  "));
    addsqlParm(", last_mm         =?", wp.itemNum("last_mm         "));
//    addsqlParm(", curr_min_amt    =?", wp.itemNum("curr_min_amt    "));
    addsqlParm(", curr_amt        =?", wp.itemNum("curr_amt        "));
//    addsqlParm(", curr_tot_cnt    =?", wp.itemNum("curr_tot_cnt    "));
    addsqlParm(", curr_cnt        =?", wp.itemNum("curr_cnt        "));
    addsqlParm(", last_cond       =?", wp.itemYn("last_cond       "));
    addsqlParm(", last_amt1       =?", wp.itemNum("last_amt1       "));
    addsqlParm(", last_cnt1       =?", wp.itemNum("last_cnt1       "));
    addsqlParm(", last_amt2       =?", wp.itemNum("last_amt2       "));
    addsqlParm(", last_cnt2       =?", wp.itemNum("last_cnt2       "));
    addsqlParm(", last_amt3       =?", wp.itemNum("last_amt3       "));
    addsqlParm(", last_cnt3       =?", wp.itemNum("last_cnt3       "));
    addsqlParm(", last_amt4       =?", wp.itemNum("last_amt4       "));
    addsqlParm(", last_cnt4       =?", wp.itemNum("last_cnt4       "));
    addsqlParm(", last_amt5       =?", wp.itemNum("last_amt5       "));
    addsqlParm(", last_cnt5       =?", wp.itemNum("last_cnt5       "));
    addsqlParm(", last_amt6       =?", wp.itemNum("last_amt6       "));
    addsqlParm(", last_cnt6       =?", wp.itemNum("last_cnt6       "));
    addsqlParm(", cond_per        =?", wp.itemYn("cond_per        "));
    addsqlParm(", per_amt         =?", wp.itemNum("per_amt         "));
    addsqlParm(", per_cnt         =?", wp.itemNum("per_cnt         "));
    addsqlParm(", air_cond        =?", wp.itemYn("air_cond        "));
    addsqlParm(", air_sup_flag_0  =?", wp.itemYn("air_sup_flag_0  "));
    addsqlParm(", air_sup_flag_1  =?", wp.itemYn("air_sup_flag_1  "));
    addsqlParm(", air_day         =?", wp.itemNum("air_day         "));
    addsqlParm(", air_amt_type    =?", wp.itemStr("air_amt_type    "));
    addsqlParm(", air_amt         =?", wp.itemNum("air_amt         "));
    addsqlParm(", air_cnt         =?", wp.itemNum("air_cnt         "));
    addsqlParm(", price_cond      =?", wp.itemYn("price_cond        "));
    addsqlParm(", price           =?", wp.itemNum("price         "));
    addsqlParm(", air_mcc_group07 =?", wp.itemStr("air_mcc_group07 "));
//    addsqlParm(", a_use_cond      =?", wp.itemYn("a_use_cond      "));
//    addsqlParm(", a_last_month    =?", wp.itemNum("a_last_month    "));
//    addsqlParm(", a_use_amt_type  =?", wp.itemStr("a_use_amt_type  "));
//    addsqlParm(", a_use_amt       =?", wp.itemNum("a_use_amt       "));
//    addsqlParm(", a_use_type      =?", wp.itemStr("a_use_type      "));
//    addsqlParm(", a_air_right     =?", wp.itemYn("a_air_right     "));
//    addsqlParm(", a_mcc_group     =?", wp.itemStr("a_mcc_group     "));
//    addsqlParm(", b_use_cond      =?", wp.itemYn("b_use_cond      "));
//    addsqlParm(", b_last_month    =?", wp.itemNum("b_last_month    "));
//    addsqlParm(", b_use_amt       =?", wp.itemNum("b_use_amt       "));
//    addsqlParm(", b_use_amt_type  =?", wp.itemStr("b_use_amt_type  "));
//    addsqlParm(", b_use_bl        =?", wp.itemYn("b_use_bl        "));
//    addsqlParm(", b_use_ca        =?", wp.itemYn("b_use_ca        "));
//    addsqlParm(", b_use_it        =?", wp.itemYn("b_use_it        "));
//    if (!wp.itemEmpty("b_use_it")) {
//      addsqlParm(", it_2_type      =?", wp.itemStr("it_2_type"));
//    } else {
//      addsqlParm(", it_2_type      =?", "");
//    }
//    addsqlParm(", b_use_ao        =?", wp.itemYn("b_use_ao        "));
//    addsqlParm(", b_use_id        =?", wp.itemYn("b_use_id        "));
//    addsqlParm(", b_use_ot        =?", wp.itemYn("b_use_ot        "));
//    addsqlParm(", c_use_cond      =?", wp.itemYn("c_use_cond      "));
//    addsqlParm(", d_use_cond      =?", wp.itemYn("d_use_cond      "));
//    addsqlParm(", d_use_right     =?", wp.itemStr("d_use_right     "));
//    addsqlParm(", d_mcc_group     =?", wp.itemStr("d_mcc_group     "));
//    addsqlParm(", e_use_cond      =?", wp.itemYn("e_use_cond      "));
    addsqlParm(", proj_date_s      =?", wp.itemStr("proj_date_s"));
    addsqlParm(", proj_date_e      =?", wp.itemStr("proj_date_e"));
    addsqlParm(", item_month_s      =?", wp.itemStr("item_month_s"));
    addsqlParm(", item_month_e      =?", wp.itemStr("item_month_e"));
    // 2020-09-26 Justin
    addsqlParm(", curr_amt_cond = ? ", wp.itemYn("curr_amt_cond"));
    addsqlParm(", curr_cnt_cond = ?", wp.itemYn("curr_cnt_cond"));
    addsqlParm(", use_cnt_cond = ?", wp.itemStr("use_cnt_cond"));
    addsqlParm(", use_max_cnt = ?", wp.itemNum("use_max_cnt"));
    addsqlParm(", curr_min_amt = ?", wp.itemNum("curr_min_amt"));
    addsqlParm(", curr_tot_cnt = ?", wp.itemNum("curr_tot_cnt"));
    // 2020-09-26 Justin
    
    addsqlParm(", crt_date        =?", wp.itemStr("crt_date        "));
    addsqlParm(", crt_user        =?", wp.itemStr("crt_user        "));
    addsqlModXXX(modUser, modUser);
    addsqlParm(" where item_no =?", itemNo);
    addsqlParm(" and proj_code =?", projCode);
    addsql2(" and apr_flag ='N'");

    sqlExec(sqlStmt(), sqlParms());

    if (sqlRowNum <= 0) {
      errmsg("update cms_right_parm error ");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    // -- 刪除主檔
    strSql = " delete cms_right_parm where item_no =? and proj_code =? and apr_flag =? ";

    setString2(1, itemNo);
    setString(projCode);
    setString(wp.itemStr2("apr_flag"));

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("delete cms_right_parm error !");
      return rc;
    }

    // -- 刪除明細
    deleteAllDetl();

    return rc;
  }

  @Override
  public int dataProc() {
    msgOK();

    copyRightParm();
    if (rc != 1)
      return rc;
    copyParmDetl();
    if (rc != 1)
      return rc;

    return rc;
  }

  void copyRightParm() {
    msgOK();

    // --搜尋主檔資料庫資料
    String sql1 = " select * from cms_right_parm where apr_flag = 'Y' "
        + " and proj_code =? and item_no = ? ";

    sqlSelect(sql1, new Object[] {wp.itemStr2("proj_code"), wp.itemStr2("item_no")});

    if (sqlRowNum <= 0) {
      errmsg("select cms_right_parm error !");
      return;
    }

    // --複製一筆主檔資料但apr_flag = 'N'
    sql2Insert("cms_right_parm");
    addsqlParm(" ?", " item_no", colStr("item_no"));
    addsqlParm(",?", ", proj_code", colStr("proj_code"));
    addsqlParm(", apr_flag", ",'N'");
    addsqlParm(",?", ", active_status", colStr("active_status"));
    addsqlParm(",?", ", proj_desc", colStr("proj_desc"));
    addsqlParm(",?", ", acct_type_flag", colNvl("acct_type_flag", "N"));
    addsqlParm(",?", ", card_hldr_flag  ", colStr("card_hldr_flag"));
    addsqlParm(",?", ", group_card_flag ", colYn("group_card_flag"));
    addsqlParm(",?", ", debut_year_flag ", colStr("debut_year_flag "));
    addsqlParm(",?", ", debut_month1    ", colNum("debut_month1    "));
    addsqlParm(",?", ", debut_sup_flag_0", colYn("debut_sup_flag_0"));
    addsqlParm(",?", ", debut_sup_flag_1", colYn("debut_sup_flag_1"));
    addsqlParm(",?", ", debut_month2    ", colNum("debut_month2"));
    addsqlParm(",?", ", debut_group_cond", colStr("debut_group_cond"));
    addsqlParm(",?", ", consume_type    ", colStr("consume_type    "));
    addsqlParm(",?", ", consume_00_cnt  ", colNum("consume_00_cnt  "));
    addsqlParm(",?", ", consume_bl      ", colYn("consume_bl      "));
    addsqlParm(",?", ", consume_ca      ", colYn("consume_ca      "));
    addsqlParm(",?", ", consume_it      ", colYn("consume_it      "));
    addsqlParm(",?", ", it_1_type", colStr("it_1_type"));
    addsqlParm(",?", ", consume_ao      ", colYn("consume_ao      "));
    addsqlParm(",?", ", consume_id      ", colYn("consume_id      "));
    addsqlParm(",?", ", consume_ot      ", colYn("consume_ot      "));
    addsqlParm(",?", ", curr_cond       ", colYn("curr_cond       "));
    addsqlParm(",?", ", choose_cond		", colYn("choose_cond	  "));
    addsqlParm(",?", ", curr_pre_month  ", colNum("curr_pre_month  "));
    addsqlParm(",?", ", last_mm  ", colNum("last_mm  "));
//    addsqlParm(",?", ", curr_min_amt", colNum("curr_min_amt"));
    addsqlParm(",?", ", curr_amt", colNum("curr_amt"));
//    addsqlParm(",?", ", curr_tot_cnt", colNum("curr_tot_cnt"));
    addsqlParm(",?", ", curr_cnt        ", colNum("curr_cnt        "));
    addsqlParm(",?", ", last_cond       ", colYn("last_cond       "));
    addsqlParm(",?", ", last_amt1       ", colNum("last_amt1       "));
    addsqlParm(",?", ", last_cnt1       ", colNum("last_cnt1       "));
    addsqlParm(",?", ", last_amt2       ", colNum("last_amt2       "));
    addsqlParm(",?", ", last_cnt2       ", colNum("last_cnt2       "));
    addsqlParm(",?", ", last_amt3       ", colNum("last_amt3       "));
    addsqlParm(",?", ", last_cnt3       ", colNum("last_cnt3       "));
    addsqlParm(",?", ", last_amt4       ", colNum("last_amt4       "));
    addsqlParm(",?", ", last_cnt4       ", colNum("last_cnt4       "));
    addsqlParm(",?", ", last_amt5       ", colNum("last_amt5       "));
    addsqlParm(",?", ", last_cnt5       ", colNum("last_cnt5       "));
    addsqlParm(",?", ", last_amt6       ", colNum("last_amt6       "));
    addsqlParm(",?", ", last_cnt6       ", colNum("last_cnt6       "));
    addsqlParm(",?", ", cond_per        ", colYn("cond_per        "));
    addsqlParm(",?", ", per_amt         ", colNum("per_amt         "));
    addsqlParm(",?", ", per_cnt         ", colNum("per_cnt         "));
    addsqlParm(",?", ", air_cond        ", colYn("air_cond        "));
    addsqlParm(",?", ", air_sup_flag_0  ", colYn("air_sup_flag_0  "));
    addsqlParm(",?", ", air_sup_flag_1  ", colYn("air_sup_flag_1  "));
    addsqlParm(",?", ", air_day         ", colNum("air_day         "));
    addsqlParm(",?", ", air_amt_type    ", colStr("air_amt_type    "));
    addsqlParm(",?", ", air_amt         ", colNum("air_amt         "));
    addsqlParm(",?", ", air_cnt         ", colNum("air_cnt         "));
    addsqlParm(",?", ", price_cond      ", colYn("price_cond        "));
    addsqlParm(",?", ", price           ", colNum("price         "));
    addsqlParm(",?", ", air_mcc_group07 ", colStr("air_mcc_group07 "));
//    addsqlParm(",?", ", a_use_cond      ", colYn("a_use_cond      "));
//    addsqlParm(",?", ", a_last_month    ", colNum("a_last_month    "));
//    addsqlParm(",?", ", a_use_amt_type  ", colStr("a_use_amt_type  "));
//    addsqlParm(",?", ", a_use_amt       ", colNum("a_use_amt       "));
//    addsqlParm(",?", ", a_use_type      ", colStr("a_use_type      "));
//    addsqlParm(",?", ", a_air_right     ", colYn("a_air_right     "));
//    addsqlParm(",?", ", a_mcc_group     ", colYn("a_mcc_group     "));
//    addsqlParm(",?", ", b_use_cond      ", colYn("b_use_cond      "));
//    addsqlParm(",?", ", b_last_month    ", colNum("b_last_month    "));
//    addsqlParm(",?", ", b_use_amt       ", colNum("b_use_amt       "));
//    addsqlParm(",?", ", b_use_amt_type  ", colStr("b_use_amt_type  "));
//    addsqlParm(",?", ", b_use_bl        ", colYn("b_use_bl        "));
//    addsqlParm(",?", ", b_use_ca        ", colYn("b_use_ca        "));
//    addsqlParm(",?", ", b_use_it        ", colYn("b_use_it        "));
//    addsqlParm(",?", ", it_2_type", colStr("it_2_type"));
//    addsqlParm(",?", ", b_use_ao        ", colYn("b_use_ao        "));
//    addsqlParm(",?", ", b_use_id        ", colYn("b_use_id        "));
//    addsqlParm(",?", ", b_use_ot        ", colYn("b_use_ot        "));
//    addsqlParm(",?", ", c_use_cond      ", colYn("c_use_cond      "));
//    addsqlParm(",?", ", d_use_cond      ", colYn("d_use_cond      "));
//    addsqlParm(",?", ", d_use_right     ", colStr("d_use_right     "));
//    addsqlParm(",?", ", d_mcc_group     ", colStr("d_mcc_group     "));
//    addsqlParm(",?", ", e_use_cond      ", colYn("e_use_cond      "));
    addsqlParm(",?", ", proj_date_s", colStr("proj_date_s"));
    addsqlParm(",?", ", proj_date_e", colStr("proj_date_e"));
    addsqlParm(",?", ", item_month_s", colStr("item_month_s"));
    addsqlParm(",?", ", item_month_e", colStr("item_month_e"));
    // 2020-09-26 Justin
    addsqlParm(",?", ", curr_amt_cond", colYn("curr_amt_cond"));
    addsqlParm(",?", ", curr_cnt_cond", colYn("curr_cnt_cond"));
    addsqlParm(",?", ", use_cnt_cond", colStr("use_cnt_cond"));
    addsqlParm(",?", ", use_max_cnt", colNum("use_max_cnt"));
    addsqlParm(",?", ", curr_min_amt", colNum("curr_min_amt"));
    addsqlParm(",?", ", curr_tot_cnt", colNum("curr_tot_cnt"));
    // 2020-09-26 Justin
    
    if (wp.itemEmpty("rowid")) {
      addsqlParm(", crt_date", "," + commSqlStr.sysYYmd);
      addsqlParm(",?", ", crt_user        ", modUser);
    } else {
      addsqlParm(",?", ", crt_date", colStr("crt_date"));
      addsqlParm(",?", ", crt_user", colStr("crt_user"));
    }
    addsqlModXXX(modUser, modPgm);

    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      errmsg("Copy cms_right_parm error !");
      return;
    }

  }

  void copyParmDetl() {
    msgOK();

    // --搜尋明細資料
    String sql1 = " select * from cms_right_parm_detl where table_id ='RIGHT' "
        + " and proj_code = ? and item_no = ? and apr_flag ='Y' ";

    sqlSelect(sql1, new Object[] {wp.itemStr2("proj_code"), wp.itemStr2("item_no")});

    if (sqlRowNum < 0) {
      errmsg("select cms_right_parm_detl error !");
    } else if (sqlRowNum == 0) {
      rc = 1;
      return;
    }

    int ilSelectCnt = 0;
    ilSelectCnt = sqlRowNum;

    // -- 複製明細資料
    strSql = " insert into cms_right_parm_detl ( " + " table_id , " + " proj_code , "
        + " item_no , " + " apr_flag , " + " data_type , " + " data_code , " + " data_code2 , " + " data_code3 , "
        + " mod_time , " + " mod_pgm " + " ) values ( " + " 'RIGHT' , " + " :proj_code , "
        + " :item_no , " + " 'N' , " + " :data_type , " + " :data_code , " + " :data_code2 , " + " :data_code3 , "
        + " sysdate , " + " :mod_pgm " + " ) ";

    for (int ii = 0; ii < ilSelectCnt; ii++) {
      setString2("proj_code", colStr(ii, "proj_code"));
      setString2("item_no", colStr(ii, "item_no"));
      setString2("data_type", colStr(ii, "data_type"));
      setString2("data_code", colStr(ii, "data_code"));
      setString2("data_code2", colStr(ii, "data_code2"));
      setString2("data_code3", colStr(ii, "data_code3"));
      setString("mod_pgm", wp.modPgm());

      sqlExec(strSql);
      if (sqlRowNum <= 0) {
        errmsg("Copy cms_right_parm_detl error !");
        return;
      }
    }
  }

  public int dataApprove() {
    msgOK();

    // --刪除已覆核主檔
    strSql =
        " delete cms_right_parm where proj_code =:proj_code and item_no =:item_no and apr_flag ='Y' ";
    var2ParmStr("proj_code");
    var2ParmStr("item_no");

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete cms_right_parm error !");
    } else
      rc = 1;

    // --刪除已覆核明細檔
    strSql = " delete cms_right_parm_detl where proj_code =:proj_code and item_no =:item_no "
        + " and table_id ='RIGHT' and apr_flag ='Y' ";

    var2ParmStr("proj_code");
    var2ParmStr("item_no");
    sqlExec(strSql);

    if (sqlRowNum < 0) {
      errmsg("delete cms_right_parm_detl error !");
    } else
      rc = 1;

    // --覆核主檔
    strSql = " update cms_right_parm set " + " apr_flag = 'Y' ,  "
        + " apr_date = to_char(sysdate,'yyyymmdd') , " + " apr_user =:apr_user "
        + " where proj_code =:proj_code " + " and item_no =:item_no ";

    setString("apr_user", wp.itemStr2("approval_user"));
    var2ParmStr("proj_code");
    var2ParmStr("item_no");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("approve cms_right_parm error !");
      return rc;
    }

    // --覆核明細檔
    strSql = " update cms_right_parm_detl set " + " apr_flag ='Y' " + " where table_id = 'RIGHT' "
        + " and proj_code =:proj_code " + " and item_no =:item_no ";

    var2ParmStr("proj_code");
    var2ParmStr("item_no");

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("approve cms_right_parm_detl error !");
    } else
      rc = 1;

    return rc;
  }

  public int updateRightParm() {
    strSql = "update cms_right_parm set" + " debut_group_cond =?, " + sqlModxxx()
        + " where item_no =?" + " and proj_code =?" + " and apr_flag =?";
    setString2(1, wp.itemStr2("debut_group_cond"));
    setString(wp.itemStr2("item_no"));
    setString(wp.itemStr2("proj_code"));
    setString(wp.itemStr2("apr_flag"));
    sqlExec(strSql);
    if (sqlRowNum > 0)
      return 1;

    sqlErr("update cms_right_parm");
    return rc;
  }


}
