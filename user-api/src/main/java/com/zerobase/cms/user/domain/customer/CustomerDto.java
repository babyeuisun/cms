package com.zerobase.cms.user.domain.customer;

import com.zerobase.cms.user.domain.model.Customer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CustomerDto {
    private String email;
    private Long id;
    private Integer balance;
    public static CustomerDto from(Customer customer){
        return new CustomerDto(customer.getEmail(), customer.getId(), customer.getBalance()==null?0: customer.getBalance());
    }
}
