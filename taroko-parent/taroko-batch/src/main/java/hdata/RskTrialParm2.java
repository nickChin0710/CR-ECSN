/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
* 109-07-06   V1.00.00  shiyuqi       updated for project coding standard     *
******************************************************************************/
package hdata;

public class RskTrialParm2 extends BaseBin {
  public String riskGroup = "";
  public String riskGroupDesc = "";
  public String rskgpRemark = "";
  public String dbrCond = "";
  public double dbrS = 0;
  public double dbrE = 0;
  public String noAssureCond = "";
  public double noAssureAmtS = 0;
  public double noAssureAmtE = 0;
  public String k34EstimateRcbalCond = "";
  public double k34EstimateRcbalS = 0;
  public double k34EstimateRcbalE = 0;
  public String k34UseRcRateCond = "";
  public double k34UseRcRateS = 0;
  public double k34UseRcRateE = 0;
  public String k34OverdueCond = "";
  public String k34OverdueFlag = "";
  public String k34OverdueBanksCond = "";
  public int k34OverdueBanksS = 0;
  public int k34OverdueBanksE = 0;
  public String k34Overdue6mmCond = "";
  public int k34Overdue6mmS = 0;
  public int k34Overdue6mmE = 0;
  public String k34Overdue12mmCond = "";
  public int k34Overdue12mmS = 0;
  public int k34Overdue12mmE = 0;
  public String k34UseCashCond = "";
  public String k34UseCashFlag = "";
  public String k34UseCash6mmCond = "";
  public int k34UseCash6mmS = 0;
  public int k34UseCash6mmE = 0;
  public String k34UseCash12mmCond = "";
  public int k34UseCash12mmS = 0;
  public int k34UseCash12mmE = 0;
  public String k34DebtCodeCond = "";
  public String k34DebtCode = "";
  public String b63NoOverdueAmtCond = "";
  public double b63NoOverdueAmtS = 0;
  public double b63NoOverdueAmtE = 0;
  public String b63OverdueCond = "";
  public String b63OverdueFlag = "";
  public String b63OverdueNopayCond = "";
  public double b63OverdueNopayS = 0;
  public double b63OverdueNopayE = 0;
  public String b63CashDueAmtCond = "";
  public double b63CashDueAmtS = 0;
  public double b63CashDueAmtE = 0;
  public String creditLimitCond = "";
  public String creditLimitSDate = "";
  public String creditLimitEDate = "";
  public String creditLimitCode = "";
  public String rcAvguseCond = "";
  public int rcAvguseMm = 0;
  public double rcAvguseRate = 0;
  public String cashUseCond = "";
  public int cashUseMm = 0;
  public int cashUseTimes = 0;
  public String limitAvguseCond = "";
  public int limitAvguseMm = 0;
  public double limitAvguseRate = 0;
  public String paymentRateCond = "";
  public int paymentRateMm = 0;
  public int paymentRateTimes = 0;
  public String noDebtCond = "";
  public int noDebtMm = 0;
  public String paymentIntCond = "";
  public String acctJrnlBalCond = "";
  public double acctJrnlBalS = 0;
  public double acctJrnlBalE = 0;
  public String trialScoreCond = "";
  public double trialScoreS = 0;
  public double trialScoreE = 0;
  public String noAssureAddCond = "";
  public double noAssureAddAmt = 0;
  public String jcic028Cond = "";
  public double jcic028S = 0;
  public double jcic028E = 0;
  public String jcic029Cond = "";
  public double jcic029S = 0;
  public double jcic029E = 0;
  public double noAssureAddAmt2 = 0;
  public String jcic036Cond = "";
  public String jcic036 = "";
  public String jcic030Cond = "";
  public String jcic030 = "";
  public String jcic031Cond = "";
  public String jcic031 = "";
  public String jcic02303Cond = "";
  public int jcic02303 = 0;
  public int jcic02303E = 0;
  public String jcic02501Cond = "";
  public int jcic02501 = 0;
  public String jcic03001Cond = "";
  public String jcic03001 = "";
  public String jcic03002Cond = "";
  public String jcic03002 = "";
  public String jcic03101Cond = "";
  public String jcic03101 = "";
  public String jcic03102Cond = "";
  public String jcic03102 = "";
  public String jcic034Cond = "";
  public String jcic034 = "";
  public String jcic032Cond = "";
  public String jcic032 = "";
  public String jcic00401Cond = "";
  public double jcic00401 = 0;
  public String jcic009Cond = "";
  public double jcic009 = 0;
  public String jcic01002Cond = "";
  public int jcic01002 = 0;
  public int jcic01002E = 0;
  public String jcic013Cond = "";
  public String jcic013 = "";
  public String jcic02301Cond = "";
  public int jcic02301 = 0;
  public int jcic02301E = 0;
  public String jcic02302Cond = "";
  public int jcic02302 = 0;
  public int jcic02302E = 0;
  public int jcic02501E = 0;
  public double jcic00401E = 0;
  public double jcic009E = 0;
  public String blockReasonCond = "";
  public String acnoBlockReason = "";
  public String crtUser = "";
  public String crtDate = "";
  public String aprFlag = "";
  public String aprDate = "";
  public String aprUser = "";

  @Override
  public void initData() {
    riskGroup = "";
    riskGroupDesc = "";
    rskgpRemark = "";
    dbrCond = "";
    dbrS = 0;
    dbrE = 0;
    noAssureCond = "";
    noAssureAmtS = 0;
    noAssureAmtE = 0;
    k34EstimateRcbalCond = "";
    k34EstimateRcbalS = 0;
    k34EstimateRcbalE = 0;
    k34UseRcRateCond = "";
    k34UseRcRateS = 0;
    k34UseRcRateE = 0;
    k34OverdueCond = "";
    k34OverdueFlag = "";
    k34OverdueBanksCond = "";
    k34OverdueBanksS = 0;
    k34OverdueBanksE = 0;
    k34Overdue6mmCond = "";
    k34Overdue6mmS = 0;
    k34Overdue6mmE = 0;
    k34Overdue12mmCond = "";
    k34Overdue12mmS = 0;
    k34Overdue12mmE = 0;
    k34UseCashCond = "";
    k34UseCashFlag = "";
    k34UseCash6mmCond = "";
    k34UseCash6mmS = 0;
    k34UseCash6mmE = 0;
    k34UseCash12mmCond = "";
    k34UseCash12mmS = 0;
    k34UseCash12mmE = 0;
    k34DebtCodeCond = "";
    k34DebtCode = "";
    b63NoOverdueAmtCond = "";
    b63NoOverdueAmtS = 0;
    b63NoOverdueAmtE = 0;
    b63OverdueCond = "";
    b63OverdueFlag = "";
    b63OverdueNopayCond = "";
    b63OverdueNopayS = 0;
    b63OverdueNopayE = 0;
    b63CashDueAmtCond = "";
    b63CashDueAmtS = 0;
    b63CashDueAmtE = 0;
    creditLimitCond = "";
    creditLimitSDate = "";
    creditLimitEDate = "";
    creditLimitCode = "";
    rcAvguseCond = "";
    rcAvguseMm = 0;
    rcAvguseRate = 0;
    cashUseCond = "";
    cashUseMm = 0;
    cashUseTimes = 0;
    limitAvguseCond = "";
    limitAvguseMm = 0;
    limitAvguseRate = 0;
    paymentRateCond = "";
    paymentRateMm = 0;
    paymentRateTimes = 0;
    noDebtCond = "";
    noDebtMm = 0;
    paymentIntCond = "";
    acctJrnlBalCond = "";
    acctJrnlBalS = 0;
    acctJrnlBalE = 0;
    trialScoreCond = "";
    trialScoreS = 0;
    trialScoreE = 0;
    noAssureAddCond = "";
    noAssureAddAmt = 0;
    jcic028Cond = "";
    jcic028S = 0;
    jcic028E = 0;
    jcic029Cond = "";
    jcic029S = 0;
    jcic029E = 0;
    noAssureAddAmt2 = 0;
    jcic036Cond = "";
    jcic036 = "";
    jcic030Cond = "";
    jcic030 = "";
    jcic031Cond = "";
    jcic031 = "";
    jcic02303Cond = "";
    jcic02303 = 0;
    jcic02501Cond = "";
    jcic02501 = 0;
    jcic03001Cond = "";
    jcic03001 = "";
    jcic03002Cond = "";
    jcic03002 = "";
    jcic03101Cond = "";
    jcic03101 = "";
    jcic03102Cond = "";
    jcic03102 = "";
    jcic034Cond = "";
    jcic034 = "";
    jcic032Cond = "";
    jcic032 = "";
    jcic00401Cond = "";
    jcic00401 = 0;
    jcic009Cond = "";
    jcic009 = 0;
    jcic01002Cond = "";
    jcic01002 = 0;
    jcic013Cond = "";
    jcic013 = "";
    jcic02301Cond = "";
    jcic02301 = 0;
    jcic02302Cond = "";
    jcic02302 = 0;
    jcic02303E = 0;
    jcic02501E = 0;
    jcic00401E = 0;
    jcic009E = 0;
    jcic01002E = 0;
    jcic02301E = 0;
    jcic02302E = 0;
    blockReasonCond = "";
    acnoBlockReason = "";
    crtUser = "";
    crtDate = "";
    aprFlag = "";
    aprDate = "";
    aprUser = "";
  }

}