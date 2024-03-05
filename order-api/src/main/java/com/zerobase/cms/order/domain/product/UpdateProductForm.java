package com.zerobase.cms.order.domain.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductForm {
    private Long id;
    private String  name;
    private String description;
    private List<UpdateProductItemForm> items;
}
