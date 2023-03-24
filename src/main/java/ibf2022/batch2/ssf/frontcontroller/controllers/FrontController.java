package ibf2022.batch2.ssf.frontcontroller.controllers;

import java.util.Random;

import javax.naming.AuthenticationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ch.qos.logback.core.model.Model;
import ibf2022.batch2.ssf.frontcontroller.services.AuthenticationService;
import ibf2022.batch2.ssf.frontcontroller.util.RedisService;
import jakarta.servlet.http.HttpSession;

@Controller
public class FrontController {

    private static final int MAX_FAILED_ATTEMPTS = 3;

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/")
    public String login(Model model, HttpSession session) {
        String disabledUsername = (String) session.getAttribute("disabledUsername");
        if (disabledUsername != null) {
            return "redirect:/view2";
		}
		generateCaptcha(model);
        return "view0";
    }

    @PostMapping("/authenticate")
    public String authenticate(@RequestParam("username") String username, @RequestParam("password") String password,
                                @RequestParam("answer") String answer, HttpSession session, Model model) {
			
			String disabledUsername = (String) session.getAttribute("disabledUsername");
			if (disabledUsername != null) {
				return "redirect:/view2";
			}

			int failedLoginAttempts;
			if (!validateCaptcha(answer, session)) {
            failedLoginAttempts++;
            generateCaptcha(model);
            ((Object) model).addAttribute("error", "Invalid captcha answer");
            return "view0";
        }

        try {
            AuthenticationService.authenticate(username, password);
            failedLoginAttempts = 0;
            return "redirect:/protected/view1.html";
        } catch (AuthenticationException e) {
            failedLoginAttempts++;
            generateCaptcha(model);
            ((Object) model).addAttribute("error", "Incorrect username and/or password");
            return "view0";

			if (getFailedAttempts(session) >= MAX_FAILED_ATTEMPTS) {
                disableUser(username);
                ((Object) model).addAttribute("disabledUser", username);
                return "view2";
            }

            generateCaptcha(model);
            return "view0";
        }
    }

    private void disableUser(String username) {
	}

	private int getFailedAttempts(HttpSession session) {
		return 0;
	}

	private boolean validateCaptcha(String answer, HttpSession session) {
        int num1 = (Integer) session.getAttribute("num1");
        int num2 = (Integer) session.getAttribute("num2");
        String op = (String) session.getAttribute("op");

        int expectedAnswer;
        switch (op) {
            case "+":
                expectedAnswer = num1 + num2;
                break;
            case "-":
                expectedAnswer = num1 - num2;
                break;
            case "*":
                expectedAnswer = num1 * num2;
                break;
            case "/":
                expectedAnswer = num1 / num2;
                break;
            default:
                return false;
        }

        int actualAnswer;
        try {
            actualAnswer = Integer.parseInt(answer);
        } catch (NumberFormatException e) {
            return false;
        }

        if (actualAnswer == expectedAnswer) {
            return true;
        } else {
            return false;
        }
    }

    private void generateCaptcha(Model model) {
        Random random = new Random();
        int num1 = random.nextInt(50) + 1;
        int num2 = random.nextInt(50) + 1;
        int opInt = random.nextInt(4);
        String op;
        switch (opInt) {
            case 0:
                op = "+";
                break;
            case 1:
                op = "-";
                break;
            case 2:
                op = "*";
                break;
            case 3:
                op = "/";
                break;
            default:
                op = "+";
                break;
        }

        ((Object) model).addAttribute("num1", num1);
        ((Object) model).addAttribute("num2", num2);
        ((Object) model).addAttribute("op", op);
    }
}

// Task 4 
private void incrementFailedAttempts(HttpSession session) {
    Integer failedAttempts = (Integer) session.getAttribute("failedLoginAttempts");
    if (failedAttempts == null) {
        failedAttempts = 0;
    }
    failedAttempts++;
    session.setAttribute("failedLoginAttempts", failedAttempts);

    if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
        String username = (String) session.getAttribute("username");
        RedisService.disableUser(username);
        
    }
}

@GetMapping("/view2")
public String view2(Model model, HttpSession session) {
    String disabledUsername = (String) session.getAttribute("disabledUsername");
    if (disabledUsername == null) {
        return "redirect:/";
    }
    long disabledUntil = (long) session.getAttribute("disabledUntil");
    long now = System.currentTimeMillis();
    if (now < disabledUntil) {
        ((Object) model).addAttribute("disabledUsername", disabledUsername);
        ((Object) model).addAttribute("disabledUntil", disabledUntil);
        return "view2";
    } else {
        session.removeAttribute("disabledUsername");
        session.removeAttribute("disabledUntil");
        return "redirect:/";
    }
}

@GetMapping("/logout")
public String logout(HttpSession session) {
	session.invalidate();
	return "redirect:/";
}

