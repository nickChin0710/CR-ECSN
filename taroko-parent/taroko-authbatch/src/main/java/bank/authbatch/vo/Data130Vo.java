package bank.authbatch.vo;

public class Data130Vo {

    public void setCardNo(String cardNo) {
        CardNo = cardNo;
    }

    public String getCardNo() {
        return CardNo;
    }

    public void setCardValidTo(String cardValidTo) {
        CardValidTo = cardValidTo;
    }

    public String getCardValidTo() {
        return CardValidTo;
    }

    public void setCardLaunchType(String cardLaunchType) {
        CardLaunchType = cardLaunchType;
    }

    public String getCardLaunchType() {
        return CardLaunchType;
    }

    public void setCardLaunchDate(String cardLaunchDate) {
        CardLaunchDate = cardLaunchDate;
    }

    public String getCardLaunchDate() {
        return CardLaunchDate;
    }

    public void setContract(String contract) {
        Contract = contract;
    }

    public String getContract() {
        return Contract;
    }

    public Data130Vo() {
        // TODO Auto-generated constructor stub
    }


    public String CardNo;
    public String CardValidTo;
    public String CardLaunchType;
    public String CardLaunchDate;
    public String Contract;

}
