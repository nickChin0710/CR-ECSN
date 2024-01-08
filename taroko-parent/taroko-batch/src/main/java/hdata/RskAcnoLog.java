package hdata;
/** rsk_acnolog
 * 2019-0829   JH    ++block_code()
 * 109-07-06   V1.00.00  shiyuqi       updated for project coding standard     *
 * */

public class RskAcnoLog extends BaseBin {

  public String kindFlag = "";
  public String cardNo = "";
  public String acnoPSeqno = "";
  public String acctType = "";
  public String idPSeqno = "";
  public String corpPSeqno = "";
  public String paramNo = "";
  public String logDate = "";
  public String logMode = "";
  public String logType = "";
  public String textfileDate = "";
  public String logReason = "";
  public String logNotReason = "";
  public double befLocAmt = 0;
  public double aftLocAmt = 0;
  public String adjLocFlag = "";
  public String fitCond = "";
  public String printCompYn = "";
  public String mailCompYn = "";
  public double securityAmt = 0;
  public String logRemark = "";
  public String blockReason = "";
  public String blockReason2 = "";
  public String blockReason3 = "";
  public String blockReason4 = "";
  public String blockReason5 = "";
  public String specStatus = "";
  public String billPrint = "";
  public double upgradeAmtWhite = 0;
  public String emendType = "";
  public String fhFlag = "";
  public double befLocCash = 0;
  public double aftLocCash = 0;
  public String sendIbmFlag = "";
  public String sendIbmDate = "";
  public String relateCode = "";
  public String relaPSeqno = "";
  public String fromSeqno = "";
  public String classCodeBef = "";
  public String classCodeAft = "";
  public String classValidDate = "";
  public String ccasMcodeBef = "";
  public String ccasMcodeAft = "";
  public String mcodeValidDate = "";
  public String smsFlag = "";
  public double acctJrnlBal = 0;
  public double cardAdjLimit = 0;
  public String cardAdjDate1 = "";
  public String cardAdjDate2 = "";
  public String userDeptNo = "";
  public String lineId = "";
  public String aprFlag = "";
  public String aprUser = "";
  public String aprDate = "";
  public String blkReviewFlag = "";
  public String blkReviewDate = "";
  public String speDelDate = "";
  public String outgoFlag = "";
  public String outgoDate = "";


  @Override
  public void initData() {
    kindFlag = "";
    cardNo = "";
    acnoPSeqno = "";
    acctType = "";
    idPSeqno = "";
    corpPSeqno = "";
    paramNo = "";
    logDate = "";
    logMode = "";
    logType = "";
    textfileDate = "";
    logReason = "";
    logNotReason = "";
    befLocAmt = 0;
    aftLocAmt = 0;
    adjLocFlag = "";
    fitCond = "";
    printCompYn = "";
    mailCompYn = "";
    securityAmt = 0;
    logRemark = "";
    blockReason = "";
    blockReason2 = "";
    blockReason3 = "";
    blockReason4 = "";
    blockReason5 = "";
    specStatus = "";
    billPrint = "";
    upgradeAmtWhite = 0;
    emendType = "";
    fhFlag = "";
    befLocCash = 0;
    aftLocCash = 0;
    sendIbmFlag = "";
    sendIbmDate = "";
    relateCode = "";
    relaPSeqno = "";
    fromSeqno = "";
    classCodeBef = "";
    classCodeAft = "";
    classValidDate = "";
    ccasMcodeBef = "";
    ccasMcodeAft = "";
    mcodeValidDate = "";
    smsFlag = "";
    acctJrnlBal = 0;
    cardAdjLimit = 0;
    cardAdjDate1 = "";
    cardAdjDate2 = "";
    userDeptNo = "";
    lineId = "";
    aprFlag = "";
    aprUser = "";
    aprDate = "";
    blkReviewFlag = "";
    blkReviewDate = "";
    speDelDate = "";
    outgoFlag = "";
    outgoDate = "";
    rowid = "";
  }

  public String blockCode() {
    return blockReason + blockReason2 + blockReason3 + blockReason4 + blockReason5;
  }

  public String[] blockAa() {
    return new String[] {blockReason, blockReason2, blockReason3, blockReason4, blockReason5,
        specStatus};
  }

}
