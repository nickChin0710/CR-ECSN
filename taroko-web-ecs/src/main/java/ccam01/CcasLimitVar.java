package ccam01;

public class CcasLimitVar {
public String acnoPSeqno="";
public String corpNo="", acctType="";
public  String acctPSeqno="";
public double cardAcctIdx=0;
public String corpPSeqno ="", idPSeqno="";
public String acnoFlag ="";
public double creditLimit =0, adjLimit=0;
public double totAmtConsume=0, totAmtPrecash=0;
public String classCode="";
public double paidConsume=0, paidPrecash=0;
public double acctJrnlBal = 0;
public double prePayAmt=0;
public double totUnpaidAmt=0;
public double ibmReceiveAmt=0;
public double txNtAmt=0;
public double unpostInstFee=0;
public double canUseLimit=0;
public double canUseSpecLimit=0;
//-----
public double cardLimit=0;
public boolean comboIndr=false;
public double creditCash=0;
public double canUseCash=0;
public String cardNo="";
public double cardTotConsume=0;
public double problemAmt = 0;
public double dbEndBal = 0;
public double totalSpecialAmt = 0;
public double specialAmt = 0;
public double overSpecialAmt = 0;
public double returnSpecAmt = 0;
public double postInstFeeSpec = 0;
public String returnDate = "";
public double returnAmt = 0;
public void initData() {
   acnoPSeqno="";
   corpNo="";
   acctType="";
   acctPSeqno="";
   cardAcctIdx=0;
   corpPSeqno ="";
   idPSeqno="";
   acnoFlag ="";
   creditLimit =0;
   adjLimit=0;
   totAmtConsume=0;
   totAmtPrecash=0;
   classCode="";
   paidConsume=0;
   paidPrecash=0;
   prePayAmt=0;
   totUnpaidAmt=0;
   ibmReceiveAmt=0;
   txNtAmt=0;
   unpostInstFee=0;
   canUseLimit=0;
//--
   cardLimit=0;
   comboIndr=false;
   creditCash=0;
   canUseCash=0;
   cardNo="";
   cardTotConsume=0;
   problemAmt = 0;
   totalSpecialAmt = 0;
   specialAmt = 0;
   overSpecialAmt = 0;
   dbEndBal = 0;
   acctJrnlBal = 0;   
   returnSpecAmt = 0;
   postInstFeeSpec = 0;
   returnAmt = 0;
}

}
