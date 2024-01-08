package secm01;

public class DeCodeSec {
  public static String userLevel(String string1) {
    String[] cde = {"A", "B", "L"};
    String[] txt = {"甲級", "乙級", "經辦"};
    return new taroko.base.CommString().decode(string1, cde, txt);
    // return ucStr.decode(s1,",1,2,4",",影本,正本,微縮影");
  }

}
