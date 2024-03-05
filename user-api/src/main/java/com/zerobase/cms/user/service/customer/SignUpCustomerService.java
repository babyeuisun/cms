package com.zerobase.cms.user.service.customer;

import com.zerobase.cms.user.domain.SignUpForm;
import com.zerobase.cms.user.domain.model.Customer;
import com.zerobase.cms.user.domain.repository.CustomerRepository;
import com.zerobase.cms.user.exception.CustomerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static com.zerobase.cms.user.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class SignUpCustomerService {
    private final CustomerRepository customerRepository;
    public Customer signUp(SignUpForm form){
        return customerRepository.save(Customer.from(form));
    }
    public boolean isEmailExist(String email){
        return customerRepository.findByEmail(email.toLowerCase(Locale.ROOT))
                        .isPresent();
    }
    @Transactional
    public void verifyEmail(String email,String code){
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(()->new CustomerException(NOT_FOUND_USER));
        if(customer.isVerify()){
            throw new CustomerException(ALREADY_VERIFY);
        } else if(!customer.getVerificationCode().equals(code)){
            throw new CustomerException(WRONG_VERIFICATION);
        } else if(customer.getVerifyExpiredAt().isBefore(LocalDateTime.now())){
            throw new CustomerException(EXPIRE_CODE);
        }
        customer.setVerify(true);
    }
    @Transactional
    public LocalDateTime changeCustomerValidateEmail(Long customerId, String verificationCode){
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if(customerOptional.isPresent()){
            LocalDateTime now = LocalDateTime.now();
            Customer customer = customerOptional.get();
            customer.setVerificationCode(verificationCode);
            customer.setVerifyExpiredAt(LocalDateTime.now().plusDays(1));
            return customer.getVerifyExpiredAt();
        }
        throw new CustomerException(NOT_FOUND_USER);
    }

}
