/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 110-01-08  V1.00.02  tanwei        修改意義不明確變量                                                                          * 
******************************************************************************/
package ecsfunc;

public class CcasIso8583 {
  public String msgheader = ""; // x12
  public String msgtype = ""; // x4
  public String bytemap = ""; // x16: second Bit Map: x16
  public String b2AcctLen = ""; // 9(19): P-2 Primary Account Number (PAN):
  public String b2AcctNo = "";
  public String b3ProcCode = ""; // 9(6)
  public String b4TranAmt = ""; // 9(12)
  public String b7DateTime = ""; // 9(10): mmdddhhmiss
  public String b11TraceNo = ""; // 9(6):
  public String b12LocalTime = ""; // 9(6): hhmiss
  public String b13LocalDate = ""; // 9(4): mmdd
  public String b14ExpirDate = ""; // 9(4): for (0310)
  public String b15SetlDate = ""; // 9(4): mmdd
  public String b17CapDate = ""; // 9(4): mmdd
  public String b18MccCode = ""; // 9(4)
  public String b19Country = ""; // 9(3)
  public String b22EntryMode = ""; // 9(3)
  public String b25ServCond = ""; // 9(2)
  public String b26PinLen = ""; // 9(2)
  public String b27Commit = ""; // ??
  public String b28TranFee = ""; // 9(7.2)
  public String b32AcqLen = ""; // x(13): 資料長度＋收單機構代碼
  public String b32AcqId = "";
  public String b35TrkiiLen = ""; // x37: 資料長度＋卡片Track 2資料
  public String b35Trackii = "";
  public String b37RefNo = ""; // x12
  public String b38ApprCode = ""; // x6:
  public String b39RespCode = ""; // x2
  public String b41TermId = ""; // x16
  public String b42MchtCode = ""; // x15
  public String b43MchtLoc = ""; // x40: 22x+13x+3x+2x
  public String b44RspLen = ""; // 9(27)
  public String b44RspData = "";
  public String b48AddLen = ""; // x(79): 076+3x+4x+2x+67x
  public String b48AddData = "";
  public String b49Currence = ""; // 9(3)
  public String b50CurrSetl = "";
  public String b52PinData = ""; // x16
  public String b53SecuCntrl = ""; // 9(16)
  public String b60PosLen = ""; // 058
  public String b60PosInfo = ""; // x4: 90+mid(bank_id,3,2)
  public String b61OtherLen = ""; // 4x
  public String b61OtherData = ""; // x22
  public String b62PostLen = ""; // 3x
  public String b62PostalCode = ""; // x10
  public String b63TokenLen = ""; // x03
  public String b63TokenFlag = ""; // x999
  public String b66SetlCode = ""; // x01
  public String b70Network = ""; // 9(03)
  public String b73ActDate = ""; // x6
  public String b90OrgData = ""; // x42
  public String b91FileCode = ""; // x: 1.新增(Add), 2.更新(Update), 3.刪除(Delete), 5.查詢(Inquiry)
  public String b95ReplAmt = ""; // x42
  public String b97NsettlAmt = ""; // x+9(17)
  public String b99SetlLen = ""; // 2x
  public String b99SetlInst = ""; // 11x
  public String b101FnameLen = ""; // x2
  public String b101FileName = ""; // 17x
  public String b120MessLen = ""; // x3
  public String b120MessData = ""; // 4x+2x+19x+12x+5x+2x+9x
  public String b121IssuerLen = ""; // x3
  public String b121Issuer = ""; // x100
  public String b122OpenLen = ""; // x3
  public String b122OpenData = ""; // x13*6
  public String b123AddrLen = ""; // x4
  public String b123AddrData = ""; // x150
  public String b125SuppLen = ""; //
  public String b125SuppData = "";
  public String b126CafLen = ""; // ATM
  public String b126CafData = ""; // ATM
  public String b127RecLen = ""; // x4: for 銀聯卡(POS user data)
  public String b127RecData = ""; // x200
  public String userExpireDate = ""; // x8

  private String[] strBit = new String[129];

  public void bytemapOn(int... num) {
    for (int ii = 0; ii < 129; ii++) {
      strBit[ii] = "0";
    }
    for (int ii : num) {
      strBit[ii] = "1";
    }
    for (int ii = 1; ii < 129; ii++) {
      bytemap += strBit[ii];
    }
  }

  public void resetVisa() {
    msgheader = "";
    msgtype = "";
    bytemap = "";
    b2AcctLen = "";
    b2AcctNo = "";
    b3ProcCode = "";
    b4TranAmt = "";
    b7DateTime = "";
    b11TraceNo = "";
    b12LocalTime = "";
    b13LocalDate = "";
    b14ExpirDate = "";
    b15SetlDate = "";
    b17CapDate = "";
    b18MccCode = "";
    b19Country = "";
    b22EntryMode = "";
    b25ServCond = "";
    b26PinLen = "";
    b27Commit = "";
    b28TranFee = "";
    b32AcqLen = "";
    b32AcqId = "";
    b35TrkiiLen = "";
    b35Trackii = "";
    b37RefNo = "";
    b38ApprCode = "";
    b39RespCode = "";
    b41TermId = "";
    b42MchtCode = "";
    b43MchtLoc = "";
    b44RspLen = "";
    b44RspData = "";
    b48AddLen = "";
    b48AddData = "";
    b49Currence = "";
    b50CurrSetl = "";
    b52PinData = "";
    b53SecuCntrl = "";
    b60PosLen = "";
    b60PosInfo = "";
    b61OtherLen = "";
    b61OtherData = "";
    b62PostLen = "";
    b62PostalCode = "";
    b63TokenLen = "";
    b63TokenFlag = "";
    b66SetlCode = "";
    b70Network = "";
    b73ActDate = "";
    b90OrgData = "";
    b91FileCode = "";
    b95ReplAmt = "";
    b97NsettlAmt = "";
    b99SetlLen = "";
    b99SetlInst = "";
    b101FnameLen = "";
    b101FileName = "";
    b120MessLen = "";
    b120MessData = "";
    b121IssuerLen = "";
    b121Issuer = "";
    b122OpenLen = "";
    b122OpenData = "";
    b123AddrLen = "";
    b123AddrData = "";
    b125SuppLen = "";
    b125SuppData = "";
    b126CafLen = "";
    b126CafData = "";
    b127RecLen = "";
    b127RecData = "";
    userExpireDate = "";
  }

}
