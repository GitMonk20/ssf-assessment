package ibf2022.batch2.ssf.frontcontroller.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/protected")
public class ProtectedController {
	public String view1() {
        return "view1";
    }

@GetMapping("/resources/**")
@PreAuthorize("isLocked()")
public String protectResources() {
    return "redirect:/";
	
	// TODO Task 5
	// Write a controller to protect resources rooted under /protected
}
}