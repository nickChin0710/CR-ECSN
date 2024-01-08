/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-20  V1.00.01  Zuwei       updated for project coding standard      *
*  110-01-19  V1.00.02  Justin        rename the method names
*  112-03-09  V1.00.03  Kevin       批次授權連線測試(false取消)  
******************************************************************************/
package bank.AuthIntf;

public class AuthData {
  public String getFullIsoCommand() {
    return fullIsoCommand;
  }

  public void setFullIsoCommand(String fullIsoCommand) {
    this.fullIsoCommand = fullIsoCommand;
  }



  String trans_type = "";// [ 1]; /* 1: regular 2:refund 3:reversal 4:代行 */
  String type_flag = "";// [ 1]; /* A: install B: mail*/
  String card_no = "";// [19];
  String expire_date = "";// [8]; /** YYYYMMDD */
  String trans_amt = "";// [12]; /* bit4 = reconcilation amt */
  String mcc_code = "";// [4]; /* bit18 mcc code */
  String mcht_no = "";// [15]; /* bit42 acceptor_id=mcht_no */
  String local_time = "";// [14]; /* yyyymmdzdhhmmss when trans_type=3 */ //Howard:不知道此欄位要放到 ISO
                         // 的哪個欄位中.....
  String org_auth_no = "";// [6]; /* when trans_type=2 need this */
  String org_ref_no = "";// [12]; /* when trans_type=3 */
  String cvv2 = "";// [4]; /* cvv2 */
  String fullIsoCommand = "";

  boolean bG_Testing = false; // 批次授權連線測試(false取消)

  public String getTransType() {
    if (bG_Testing)
      trans_type = "1";
    return trans_type;
  }

  public void setTransType(String trans_type) {
    this.trans_type = trans_type;
  }

  public String getTypeFlag() {
    if (bG_Testing)
      type_flag = "A";
    return type_flag;
  }

  public void setTypeFlag(String type_flag) {
    this.type_flag = type_flag;
  }

  public String getCardNo() {
    if (bG_Testing)
      card_no = "5542123456780987";

    return card_no;
  }

  public void setCardNo(String card_no) {
    this.card_no = card_no;
  }

  public String getExpireDate() {
    if (bG_Testing)
      expire_date = "20181231";
    return expire_date;
  }

  public void setExpireDate(String expire_date) {
    this.expire_date = expire_date;
  }

  public String getTransAmt() {
    if (bG_Testing)
      trans_amt = "123";

    return trans_amt;
  }

  public void setTransAmt(String trans_amt) {
    this.trans_amt = trans_amt;
  }

  public String getMccCode() {
    if (bG_Testing)
      mcc_code = "9988";

    return mcc_code;
  }

  public void setMccCode(String mcc_code) {
    this.mcc_code = mcc_code;
  }

  public String getMchtNo() {
    if (bG_Testing)
      mcht_no = "1234567890";

    return mcht_no;
  }

  public void setMchtNo(String mcht_no) {
    this.mcht_no = mcht_no;
  }

  public String getLocalTime() {
    if (bG_Testing)
      local_time = "20170925133020";

    return local_time;
  }

  public void setLocalTime(String local_time) {
    this.local_time = local_time;
  }

  public String getOrgAuthNo() {
    if (bG_Testing)
      org_auth_no = "666888";

    return org_auth_no;
  }

  public void setOrgAuthNo(String org_auth_no) {
    this.org_auth_no = org_auth_no;
  }

  public String getOrgRefNo() {
    if (bG_Testing)
      org_ref_no = "223344556677";

    return org_ref_no;
  }

  public void setOrgRefNo(String org_ref_no) {
    this.org_ref_no = org_ref_no;
  }

  public String getCvv2() {
    if (bG_Testing)
      cvv2 = "9911";

    return cvv2;
  }

  public void setCvv2(String cvv2) {
    this.cvv2 = cvv2;
  }



  public AuthData() {
    // TODO Auto-generated constructor stub

  }

}
