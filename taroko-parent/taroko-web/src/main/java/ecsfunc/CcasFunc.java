/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
*                                                                            *  
******************************************************************************/
package ecsfunc;

import ofcapp.BasePage;

public class CcasFunc extends BasePage {

  public int ccasApprove(String aprUser, String aprPasswd) {

    if (wp == null) {
      errmsg("程式設計錯誤; wp is NULL");
      return -1;
    }
    if (empty(aprUser) || empty(aprPasswd)) {
      errmsg("覆核主管及密碼: 不可空白");
      return -1;
    }

    String sql1 = "update ";
    return 1;
    /*
     * dw_8.accepttext() ls_userid = trim(dw_8.getitemstring(1,"user_id")) ls_befor_pass=
     * trim(dw_8.getitemstring(1,"user_passwd")) IF ls_userid="" or ls_befor_pass="" THEN
     * f_show_msg("Stop...必須輸入放行人員代號..") return false END IF IF ls_userid = gs_userid then
     * f_show_msg("Stop...自己不可是放行人員..無法作業~") return false END IF
     * 
     * SELECT eff_end_date,chker_flag,user_passwd,auth_amt_pct,usr_amt_pct,usr_end_date INTO
     * :ls_eff_end_date,:ls_flag,:ls_pass,:ld_auth_amt_pct,:ld_usr_amt_pct,:ls_usr_end_date FROM
     * USER_BASE WHERE USER_ID=:ls_userid ; IF sqlca.sqlcode <> 0 then
     * f_show_msg("Stop...非放行人員..無法作業~") return false END IF IF isnull(ls_eff_end_date) then
     * ls_eff_end_date='00000000' IF isnull(ls_flag) then ls_flag='N' IF isnull(ls_pass) then
     * ls_pass='' IF ls_flag <> '1' then f_show_msg("Stop...非放行主管..無法作業~") return false END IF
     * //todatex = string(today(), "yyyymmdd") f_get_sysdatetime(ls_date, ls_time) IF
     * ls_eff_end_date < ls_date then f_show_msg("Stop...此放行人員密碼效期已過..無法作業~") return false END IF IF
     * ls_usr_end_date < ls_date then f_show_msg("Stop...此放行人員效期已過..無法作業~") return false END IF
     * choose case ls_flag case 'N' f_show_msg("Stop...非放行主管..無法作業~") return false case '2' // 職員
     * f_show_msg("Stop...非放行主管..無法作業~") return false // dw_3.AcceptTExt() // lm_tot_amt_month =
     * dw_3.Object.tot_amt_month[1] // IF lm_tot_amt_month > ld_auth_amt_pct THEN //
     * f_show_msg("放大倍數超過放行人員權限("+String(ld_auth_amt_pct)+")倍") // RETURN FALSE // END IF //
     * ls_after_pass = Space(Len(ls_befor_pass)) // eds_encodepsw(ls_befor_pass, ls_after_pass) //
     * IF ls_after_pass <> ls_pass then // f_show_msg("Stop...放行人員密碼錯誤~..無法作業~") //
     * //============98/08/06 密碼錯誤寫檔============BEG // ii_err_cnt = ii_err_cnt + 1 // ls_err_msg =
     * is_prd_type+& // '
     * 產品~類別臨調:放行人員='+ls_userid+'作業~人員='+gs_userid+'密碼第'+String(ii_err_cnt)+'次錯誤~' //
     * f_err_log(ii_err_cnt, ls_err_msg, this.classname()) // //============98/08/06
     * 密碼錯誤寫檔============END // return false // END IF case '1' // 主管 long ll_seq_no
     * dw_3.AcceptTExt() lm_tot_amt_month = dw_3.Object.tot_amt_month[1] IF lm_tot_amt_month >
     * ld_auth_amt_pct THEN f_show_msg("放大倍數超過主管人員權限("+String(ld_auth_amt_pct)+")倍") RETURN FALSE
     * END IF select chker_passwd, seq_no into :ls_pass, :ll_seq_no from passwd_list where user_id =
     * :ls_userid ; if sqlca.sqlcode = 100 then f_show_msgbox("警告訊息","主管未產生放行密碼..無法作業~") RETURN
     * FALSE end if ls_after_pass = Space(Len(ls_befor_pass)) eds_encodepsw(ls_befor_pass,
     * ls_after_pass) IF ls_after_pass <> ls_pass then f_show_msgbox("警告訊息","主管放行密碼錯誤~..無法作業~")
     * //============98/08/06 密碼錯誤寫檔============BEG ii_err_cnt = ii_err_cnt + 1 ls_err_msg =
     * is_prd_type+& ' 產品~類別臨調:放行人員='+ls_userid+'作業~人員='+gs_userid+'密碼第'+String(ii_err_cnt)+'次錯誤~'
     * f_err_log(ii_err_cnt, ls_err_msg, this.classname()) //============98/08/06
     * 密碼錯誤寫檔============END return false END IF delete passwd_list where user_id = :ls_userid and
     * seq_no = :ll_seq_no;
     */

  }

}
