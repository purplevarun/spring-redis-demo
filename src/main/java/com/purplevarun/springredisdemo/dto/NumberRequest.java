package com.purplevarun.springredisdemo.dto;

import jakarta.validation.constraints.NotNull;

public class NumberRequest {

    @NotNull(message = "value must not be null")
    private Integer value;

    public NumberRequest() {}

    public NumberRequest(Integer value) {
        this.value = value;
    }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
}
