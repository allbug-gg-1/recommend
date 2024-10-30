package com.sofm.recommend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProvinceCityDto implements Serializable {
    private String province;
    private String city;

    public static ProvinceCityDto of(String province, String city) {
        return new ProvinceCityDto(province, city);
    }

    public static ProvinceCityDto of() {
        return new ProvinceCityDto();
    }

    public void withProvince(String province) {
        this.setProvince(province);
    }

    public void withCity(String city) {
        this.setCity(city);
    }
}
