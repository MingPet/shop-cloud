package com.fh.shop.api.config;

import com.fh.shop.api.filter.Filter1;
import com.fh.shop.api.filter.Filter2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    //@Bean
    public Filter1 filter1(){
        return new Filter1();
    }



    //@Bean
    public Filter2 filter2(){
        return new Filter2();
    }

}
