package com.shippix.Payment.config;

import com.shippix.Payment.dto.PaymentResponse;
import com.shippix.Payment.model.Payment;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig
{
    @Bean
    public ModelMapper modelMapper()
    {
        ModelMapper mapper = new ModelMapper();

        // Custom mapping Payment -> PaymentResponse
        mapper.addMappings(new PropertyMap<Payment, PaymentResponse>()
        {
            @Override
            protected void configure() {
                map().setOrderRequestId(source.getOrderRequest().getReqId());
                map().setAmount(source.getAmount());
                map().setCode(source.getCode());
            }
        });

        return mapper;
    }
}
