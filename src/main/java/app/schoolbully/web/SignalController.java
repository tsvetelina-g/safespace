package app.schoolbully.web;

import app.schoolbully.model.entity.Signal;
import app.schoolbully.model.entity.User;
import app.schoolbully.service.SignalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SignalController {
    private final SignalService signalService;

    @GetMapping("/signal")
    public String showSignalForm(Model model, Authentication authentication) {
        model.addAttribute("signal", new SignalForm());
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();
        model.addAttribute("isAuthenticated", isAuthenticated);
        
        // If authenticated, get user info for display
        if (isAuthenticated) {
            var userDetails = (app.schoolbully.security.CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            model.addAttribute("currentUser", user);
            model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
        }
        
        return "signal";
    }

    @PostMapping("/signal")
    public String submitSignal(@ModelAttribute SignalForm form, 
                              @RequestParam(required = false) String studentName,
                              @RequestParam(required = false) String studentPhoneNumber,
                              Authentication authentication,
                              Model model) {
        User student = null;
        boolean anonymous = true;
        String finalStudentName = null;
        String finalStudentPhone = null;
        
        // Check if user is authenticated
        if (authentication != null && authentication.isAuthenticated()) {
            var userDetails = (app.schoolbully.security.CustomUserDetails) authentication.getPrincipal();
            student = userDetails.getUser();
            
            // If logged in: check if they want to submit anonymously
            if (form.isAnonymous()) {
                // User chose to submit anonymously even though logged in
                anonymous = true;
                finalStudentName = null;
                finalStudentPhone = null;
                student = null; // Don't link to user account
            } else {
                // User wants to submit with their account info
                anonymous = false;
                finalStudentName = student.getFirstName() + " " + student.getLastName();
                finalStudentPhone = student.getPhoneNumber();
            }
        } else {
            // If not logged in: anonymous = true by default, don't store name
            anonymous = true;
            finalStudentName = null;
            finalStudentPhone = null;
        }

        Signal signal = signalService.createSignal(
            form.getBodyText(),
            anonymous,
            finalStudentName,
            finalStudentPhone,
            student
        );

        // Add user info to model for display
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();
        model.addAttribute("isAuthenticated", isAuthenticated);
        if (isAuthenticated && student != null) {
            model.addAttribute("currentUser", student);
            model.addAttribute("userFullName", student.getFirstName() + " " + student.getLastName());
        }

        model.addAttribute("signal", signal);
        model.addAttribute("showResult", true);
        return "signal";
    }

    // Simple form DTO
    public static class SignalForm {
        private String bodyText;
        private boolean anonymous;
        private String whoIsBullying;

        public String getBodyText() { return bodyText; }
        public void setBodyText(String bodyText) { this.bodyText = bodyText; }
        public boolean isAnonymous() { return anonymous; }
        public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
        public String getWhoIsBullying() { return whoIsBullying; }
        public void setWhoIsBullying(String whoIsBullying) { this.whoIsBullying = whoIsBullying; }
    }
}

