package org.oskari.example;

import java.util.ArrayList;
import java.util.List;

public class RiskUP {
    List <RiskInfoUP> risk_info;
    public String fclass;
    public String location;
    public Integer scenario;
    public Long oskari_code;
    public RiskUP() {
        risk_info=new ArrayList<>();
    }
}
