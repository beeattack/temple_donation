package com.jdea.donationinfo.service;

import org.springframework.stereotype.Service;

@Service
public class UtilsService {
  private static final String[] DIGIT_TH = { "๐", "๑", "๒", "๓", "๔", "๕", "๖", "๗", "๘", "๙" };
  private String valueText;  


  public String convertEngToThaiYear(String ddmmyyyyDate){
    String res = "ddmmyyyyDate";
    try{
      String[] splitDate = ddmmyyyyDate.split("/");
      int intYear = Integer.parseInt(splitDate[2]);
      res = splitDate[0] + "/" + splitDate[1]  + "/" + (intYear + 543);
    }
    catch(Exception ex){
      System.out.println(ex.getMessage());
    }
    return res;
  }

  public String convertThaiToEngYear(String ddmmyyyyDate){
    String res = "ddmmyyyyDate";
    try{
      String[] splitDate = ddmmyyyyDate.split("/");
      int intYear = Integer.parseInt(splitDate[2]);
      res = splitDate[0] + "/" + splitDate[1]  + "/" + (intYear - 543);
    }
    catch(Exception ex){
      System.out.println(ex.getMessage());
    }
    return res;
  }

  // Thai number converter
  // ···········Methods·············· 
  public String getText(double amount) {
    this.valueText = getThaiNumber(String.valueOf(amount));  
    return this.valueText;  
}  

public String getText(float amount) {
    this.valueText = getThaiNumber(String.valueOf(amount));  
    return this.valueText;  
}  

public String getText(int amount) {  
    this.valueText = getThaiNumber(String.valueOf(amount));  
    return this.valueText;  
}  

public String getText(long amount) {
    this.valueText = getThaiNumber(String.valueOf(amount));  
    return this.valueText;  
}  

public String getText(String amount) {  
    this.valueText = getThaiNumber(amount.trim());  
    return this.valueText;  
}  

public String getText(Number amount) {  
    this.valueText = getThaiNumber(String.valueOf(amount));  
    return this.valueText;  
}  

private static String getThaiNumber(String amount) {  
    if(amount == null || amount.isEmpty()) return "";

    StringBuilder sb = new StringBuilder();
    for(char c : amount.toCharArray()){
        if(Character.isDigit(c)){
            String index = DIGIT_TH[Character.getNumericValue(c)].toString();
            sb.append(index);
        } else {
            sb.append(c);
        }
    }
    return sb.toString();  
}  
}
