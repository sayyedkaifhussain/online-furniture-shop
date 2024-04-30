package com.jtspringproject.JtSpringProject.controller;

import com.jtspringproject.JtSpringProject.models.Cart;
import com.jtspringproject.JtSpringProject.models.Product;
import com.jtspringproject.JtSpringProject.models.User;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.jtspringproject.JtSpringProject.services.cartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.jtspringproject.JtSpringProject.services.userService;
import com.jtspringproject.JtSpringProject.services.productService;


@Controller
public class UserController {

    @Autowired
    private userService userService;

    @Autowired
    private productService productService;

    @Autowired
    private cartService cartService;

    @GetMapping("/register")
    public String registerUser() {
        return "register";
    }

    @GetMapping("/buy")
    public String buy() {
        return "buy";
    }


    @GetMapping("/")
    public String userlogin(Model model) {

        return "userLogin";
    }

    @RequestMapping(value = "userloginvalidate", method = RequestMethod.POST)
    public ModelAndView userlogin(@RequestParam("username") String username, @RequestParam("password") String pass,
                                  Model model, HttpServletResponse res) {

        System.out.println(pass);
        User u = this.userService.checkLogin(username, pass);
        System.out.println(u.getUsername());

        if (username.equals(u.getUsername())) {

            res.addCookie(new Cookie("username", u.getUsername()));
            ModelAndView mView = new ModelAndView("index");
            mView.addObject("user", u);
            List<Product> products = this.productService.getProducts();

            if (products.isEmpty()) {
                mView.addObject("msg", "No products are available");
            } else {
                mView.addObject("username", u.getUsername());
                mView.addObject("products", products);
            }
            return mView;

        } else {
            ModelAndView mView = new ModelAndView("userLogin");
            mView.addObject("msg", "Please enter correct email and password");
            return mView;
        }

    }


    @GetMapping("/user/products")
    public ModelAndView getproduct() {

        ModelAndView mView = new ModelAndView("uproduct");

        List<Product> products = this.productService.getProducts();

        if (products.isEmpty()) {
            mView.addObject("msg", "No products are available");
        } else {
            mView.addObject("products", products);
        }
        return mView;
    }

    @RequestMapping(value = "newuserregister", method = RequestMethod.POST)
    public ModelAndView newUseRegister(@ModelAttribute User user) {
        // Check if username already exists in database
        boolean exists = this.userService.checkUserExists(user.getUsername());

        if (!exists) {
            System.out.println(user.getEmail());
            user.setRole("ROLE_NORMAL");
            this.userService.addUser(user);
            System.out.println("New user created: " + user.getUsername());
            ModelAndView mView = new ModelAndView("userLogin");
            return mView;
        } else {
            System.out.println("New user not created - username taken: " + user.getUsername());
            ModelAndView mView = new ModelAndView("register");
            mView.addObject("msg", user.getUsername() + " is taken. Please choose a different username.");
            return mView;
        }
    }

    @GetMapping("profileDisplay")
    public ModelAndView profileDisplay(@RequestParam int id) {
        User user = userService.getUserById(id);

        ModelAndView model = new ModelAndView("updateProfile");
        model.addObject("userid", user.getId());
        model.addObject("username", user.getUsername());
        model.addObject("email", user.getEmail());
        model.addObject("password", user.getPassword());
        model.addObject("address", user.getAddress());

        return model;
    }

    @RequestMapping(value = "updateuser", method = RequestMethod.POST)
    public ModelAndView updateUserProfile(@RequestParam("userid") int userid, @RequestParam("username") String username,
                                          @RequestParam("email") String email,
                                          @RequestParam("password") String password,
                                          @RequestParam("address") String address) {
        User user = userService.getUserById(userid);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setAddress(address);
        this.userService.addUser(user);
        return new ModelAndView("userLogin");
    }

    @GetMapping("products/addtocart")
    public ModelAndView addToCart(@RequestParam("productId") int productId, @RequestParam int id,
                                  HttpServletResponse response) {
        Product product = productService.getProduct(productId);
        User user = userService.getUserById(id);
        Cart cart = cartService.getUserCart(user);
        if (cart.getId() == 0) {
            cartService.saveOrUpdateCart(cart);
        }
        boolean productExistsInCart = cart.getProducts().stream()
                                          .anyMatch(p -> p.getId() == productId);
        if (!productExistsInCart) {
            cart.getProducts().add(product);
            cartService.saveOrUpdateCart(cart);
        }
        ModelAndView mav = new ModelAndView("cartModel");
        mav.addObject("cartSuccess", true);
        return mav;
    }

    @GetMapping("/carts")
    public ModelAndView getCartDetail(@RequestParam int id) {
        ModelAndView mv = new ModelAndView("cartproduct");
        User user = userService.getUserById(id);
        Cart cart = cartService.getUserCart(user);
        mv.addObject("products", cart.getProducts());
        mv.addObject("customer", cart.getCustomer());
        return mv;
    }

    @GetMapping("/cart/delete")
    public ModelAndView removeFromCart(@RequestParam("id") int productId, @RequestParam int userId) {
        User user = userService.getUserById(userId);
        Cart cart = cartService.getUserCart(user);
        cart.getProducts().removeIf(product -> product.getId() == productId);
        cartService.saveOrUpdateCart(cart);
        return new ModelAndView("redirect:/carts?id=" + userId);
    }
}
