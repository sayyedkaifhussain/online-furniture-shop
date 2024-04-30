package com.jtspringproject.JtSpringProject.services;

import com.jtspringproject.JtSpringProject.dao.cartDao;
import com.jtspringproject.JtSpringProject.models.Cart;
import com.jtspringproject.JtSpringProject.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class cartService {
    @Autowired
    private cartDao cartDao;

    public Cart addCart(Cart cart) {
        return cartDao.addCart(cart);
    }

//    public Cart getCart(int id) {
//        return cartDao.getCart(id);
//    }

    public List<Cart> getCarts() {
        return this.cartDao.getCarts();
    }

    public void updateCart(Cart cart) {
        cartDao.updateCart(cart);
    }

    public void deleteCart(Cart cart) {
        cartDao.deleteCart(cart);
    }

    public Cart getUserCart(User user) {
        Optional<Cart> optionalCart = Optional.ofNullable(cartDao.getCartsByCustomerID(user.getId()));
        return optionalCart.orElseGet(() -> {
            Cart cart = new Cart();
            cart.setCustomer(user);
            return cart;
        });
    }

    public void saveOrUpdateCart(Cart cart) {
        cartDao.saveOrUpdate(cart);
    }
}
