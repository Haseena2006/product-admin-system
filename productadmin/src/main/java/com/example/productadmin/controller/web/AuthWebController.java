package com.example.productadmin.controller.web;

import com.example.productadmin.dto.RegisterRequest;
import com.example.productadmin.exception.DuplicateUserException;
import com.example.productadmin.exception.InvalidRequestException;
import com.example.productadmin.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthWebController {

    private final RegistrationService registrationService;

    public AuthWebController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (isLoggedIn(authentication)) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Authentication authentication, Model model) {
        if (isLoggedIn(authentication)) {
            return "redirect:/dashboard";
        }
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            registrationService.register(request);
        } catch (DuplicateUserException ex) {
            bindingResult.rejectValue(ex.getField(), "duplicate", ex.getMessage());
            return "register";
        } catch (InvalidRequestException ex) {
            bindingResult.rejectValue(ex.getField(), "invalid", ex.getMessage());
            return "register";
        }
        redirectAttributes.addFlashAttribute("success", "Registration successful. Please log in.");
        return "redirect:/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    private boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
