package com.zerobase.cms.order.application;


import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.model.ProductItem;
import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.exception.ErrorCode;
import com.zerobase.cms.order.service.CartService;
import com.zerobase.cms.order.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartApplication {

        private final CartService cartService;
        private final ProductSearchService productSearchService;

        public Cart addCart(Long customerId, AddProductCartForm form){
            Product product = productSearchService.getByProductId(form.getId());
            if(product == null){
                throw new CustomException(ErrorCode.NOT_FOUND_PRODUCT);
            }

            Cart cart = cartService.getCart(customerId);

            if(!addAble(cart,product,form)){
                throw new CustomException(ErrorCode.ITEM_COUNT_NOT_ENOUGH);
            }
            return cartService.addCart(customerId, form);
        }
        public Cart updateCart(Long customerId,Cart cart){
         cartService.putCart(customerId, cart);
         return getCart(customerId);
        }

        public Cart getCart(Long customerId) {
            Cart cart = refreshCart(cartService.getCart(customerId));
            cartService.putCart(cart.getCustomerId(),cart);
            Cart returnCart = new Cart();
            returnCart.setCustomerId(customerId);
            returnCart.setProducts(cart.getProducts());
            returnCart.setMessages(cart.getMessages());
            cart.setMessages(new ArrayList<>());
            cartService.putCart(customerId,cart);
            return returnCart;
        }
        public void clearCart(Long customerId){
            cartService.putCart(customerId,null);
        }

        protected Cart refreshCart(Cart cart){
            Map<Long,Product> productMap = productSearchService.getListByProductIds(
                    cart.getProducts().stream().map(Cart.Product::getId).collect(Collectors.toList()))
                    .stream()
                    .collect(Collectors.toMap(Product::getId,product -> product));

            for(int i = 0 ;i<cart.getProducts().size();i++){
                Cart.Product cartProduct  = cart.getProducts().get(i);
                Product p = productMap.get(cartProduct.getId());
                if(p==null){
                    cart.getProducts().remove(cartProduct);
                    i--;
                    cart.addMessage(cartProduct.getName() + "상품이 삭제되었습니다");
                    continue;
                    }

                Map<Long,ProductItem> productItemMap = p.getProductItems().stream()
                        .collect(Collectors.toMap(ProductItem::getId,productItem ->productItem));


                List<String> tmpMessages = new ArrayList<>();
                for(int j = 0 ; j<cartProduct.getItems().size();j++){
                    Cart.ProductItem cartProductItem = cartProduct.getItems().get(j);
                    ProductItem pi = productItemMap.get(cartProductItem.getId());
                    if(pi==null){
                        cartProduct.getItems().remove(cartProductItem);
                        j--;
                        tmpMessages.add(cartProduct.getName() +"옵션이 삭제되었습니다");
                        continue;
                    }

                    boolean isPriceChanged = false, isCountNotEnough = false;
                    if (!cartProductItem.getPrice().equals(pi.getPrice())){
                        isPriceChanged = true;
                        cartProductItem.setPrice(pi.getPrice());
                    }
                    if(cartProductItem.getCount()>pi.getCount()){
                        isCountNotEnough = true;
                        cartProductItem.setCount(pi.getCount());
                    }
                    if(isPriceChanged && isCountNotEnough){
                        tmpMessages.add(cartProduct.getName() +"가격과 수량이 변경되었습니다 ");

                    }else if(isPriceChanged){
                        tmpMessages.add(cartProduct.getName() +"가격이 변경되었습니다 ");

                    } else if (isCountNotEnough) {
                        tmpMessages.add(cartProduct.getName() +"수량이 변경되었습니다 ");
                    }
                }

                if(cartProduct.getItems().size() == 0 ){
                    cart.getProducts().remove(cartProduct);
                    i--;
                    cart.addMessage(cartProduct.getName() +"상품이 삭제되었습니다");
                }
                else if(tmpMessages.size()>0){
                    StringBuilder builder = new StringBuilder();
                    builder.append(cartProduct.getName() + "상품의 변동사항 :");
                    for(String message : tmpMessages){
                        builder.append(message);
                        builder.append(" , ");
                    }
                    cart.addMessage(builder.toString());
                    }
            }
            return cart;
        }

        private boolean addAble(Cart cart, Product product ,AddProductCartForm form){
            Cart.Product cartProduct = cart.getProducts().stream().filter(p -> p.getId().equals(form.getId()))
                    .findFirst().orElse(Cart.Product.builder().id(product.getId())
                            .items(Collections.emptyList()).build());
            Map<Long,Integer> cartItemCountMap = cartProduct.getItems().stream()
                    .collect(Collectors.toMap(Cart.ProductItem::getId,Cart.ProductItem::getCount));

            Map<Long,Integer> currentItemCountMap = product.getProductItems().stream()
                            .collect(Collectors.toMap(ProductItem::getId,ProductItem::getCount));
            return form.getItems().stream().noneMatch(
                    formItem -> {
                      Integer cartCount =   cartItemCountMap.get(formItem.getId());
                      if(cartCount == null){
                          cartCount =0;
                      }
                      Integer currentCount = currentItemCountMap.get(formItem.getId());
                      return formItem.getCount() + cartCount > currentCount;
                      }) ;

        }


}
