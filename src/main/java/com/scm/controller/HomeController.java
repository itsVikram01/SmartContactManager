package com.scm.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.scm.dao.UserRepository;
import com.scm.entities.User;
import com.scm.helper.Message;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	// home handler
	@GetMapping("/")
	public String home(Model model) {
		System.out.println("Inside home Handler................");

		model.addAttribute("title", "Home-Smart Contact Manager");
		return "home";
	}

	// about handler
	@GetMapping("/about")
	public String about(Model model) {
		System.out.println("Inside about Handler................");

		model.addAttribute("title", "About-Smart Contact Manager");
		return "about";
	}

	// signup handler
	@GetMapping("/signup")
	public String signup(Model model) {
		System.out.println("Inside signup Handler................");

		model.addAttribute("title", "Register-Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	// handler for registering user
	@PostMapping("/register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult,
			@RequestParam(value = "agreement", defaultValue = "false") Boolean agreement, Model model,
			HttpSession session) {
		System.out.println("Inside registerUser Handler................");
		try {
			if (!agreement) {
				System.out.println("You have not agree term and condition!!");
				throw new Exception("You have not agree term and condition!!");
			}
			if (bindingResult.hasErrors()) {
				System.out.println("ERROR : " + bindingResult.toString());
				model.addAttribute("user", user);
				return "signup";
			}

			// assign role
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImgUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println("user : " + user);
			System.out.println("agreement : " + agreement);

			this.userRepository.save(user);

			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered!!", "alert-success"));
			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong!! " + e.getMessage(), "alert-danger"));
			return "signup";
		}
	}

	// my login handler 
	@GetMapping("/signin")
	public String login(Model model) {
		System.out.println("Inside login Handler................");
		model.addAttribute("title", "Login-Smart Contact Manager");
		return "login";
	}
}
