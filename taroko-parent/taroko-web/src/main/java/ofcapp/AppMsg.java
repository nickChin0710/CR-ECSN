/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
* 110-01-08  V1.00.02  tanwei        修改意義不明確變量                                                                          * 
******************************************************************************/
package ofcapp;
/** 共用信息
 * 2019-0808   JH    opt_approve
 * 2018-0326:	JH		modify
 */

@SuppressWarnings({"unchecked", "deprecation"})
public class AppMsg {

  // -Error Message-
  public final String errCondNodata = "此條件查無資料";
  public final String errStrend = "起值不可大於迄值";
  public final String errDataNodata = "此條件查無資料";
  public final String errNotFind = "資料不存在";
  public final String otherModify = "資料已被修改, 或不存在";
  public final String optApprove = "請點選欲覆核資料";
  public final String optProcess = "請點選欲處理資料";

  public String sqlcodeToMesg(String strName) {
    // SQLCODE=-803, SQLSTATE=23505, SQLERRMC=1;
    if (strName.indexOf("SQLCODE=-803, SQLSTATE=23505, SQLERRMC=1") > 0) {
      return "資料己存在不可新增";
    }

    return strName;
  }

}
