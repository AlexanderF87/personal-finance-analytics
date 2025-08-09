package de.finance.analytics.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home Controller for Personal Finance Analytics Dashboard
 * Banking/Fintech Portfolio Project - Senior Java Developer
 */
@Controller
public class HomeController {

    /**
     * Banking Dashboard Home Page
     * Shows financial overview and navigation
     */
    @GetMapping("/")
    public String home(Model model) {
        // Add basic dashboard data
        model.addAttribute("appName", "Personal Finance Analytics");
        model.addAttribute("version", "1.0.0-MVP");
        model.addAttribute("description", "Banking Data Processing & Analytics Platform");

        // Banking-specific welcome message
        model.addAttribute("welcomeMessage",
                "Welcome to your Personal Finance Analytics Platform");
        model.addAttribute("subtitle",
                "Import your banking data and gain financial insights");

        return "dashboard/home";
    }

    /**
     * Banking Data Import Page
     */
    @GetMapping("/import")
    public String importData(Model model) {
        model.addAttribute("title", "Import Banking Data");
        model.addAttribute("supportedFormats",
                new String[]{"CSV", "MT940", "PDF", "CAMT.053"});
        model.addAttribute("supportedBanks",
                new String[]{"Sparkasse", "Volksbank", "Deutsche Bank", "DKB", "ING"});

        return "banking/import";
    }

    /**
     * Analytics Dashboard
     */
    @GetMapping("/analytics")
    public String analytics(Model model) {
        model.addAttribute("title", "Financial Analytics");
        model.addAttribute("description",
                "Interactive charts and trend analysis");

        return "analytics/dashboard";
    }
}