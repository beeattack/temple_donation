package com.jdea.donationinfo.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.jdea.donationinfo.model.DonationInfo;
import com.jdea.donationinfo.model.User;
import com.jdea.donationinfo.repository.DonationInfoRepository;
import com.jdea.donationinfo.repository.UserRepository;
import com.jdea.donationinfo.service.ReportService;
import com.jdea.donationinfo.service.UtilsService;

import org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi.ecCVCDSA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.event.TransactionalApplicationListenerMethodAdapter;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Controller
public class MainController {

    private String siteName;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private DonationInfoRepository donationInfoRepository;

    @Autowired
    private ReportService service;

    @Autowired
    private UtilsService utilsService;

    MainController() {
        this.siteName = "วัดดอนโฆสิตาราม";
    }

    private Boolean isAuthen(Cookie[] cookies){
        Map<String, String> cookieMap = new HashMap<>();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie.getValue());
            }
        }

        if(cookieMap.get("username") == null || cookieMap.get("username").trim() == ""){
            return false;
        }
        else{
            return true;
        }
    }

    @GetMapping("/")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if(!this.isAuthen(cookies)){
            ModelAndView viewData = new ModelAndView("redirect:/login");
            return viewData;
        }

        ModelAndView viewData = new ModelAndView("index");
        viewData.addObject("templeName", this.siteName);
        viewData.addObject("pageName", "index");
        viewData.addObject("title", "Landing pagae: ");
        return viewData;
    }

    @GetMapping("/login")
    public ModelAndView login(
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response) {
        ModelAndView viewData = new ModelAndView("login");
        viewData.addObject("pageName", "login");
        viewData.addObject("title", "กรุณาล๊อกอิน");
        return viewData;
    }

    @PostMapping("/authenticate")
    public String authenUser(
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        Cookie[] cookies = request.getCookies();
        // do authen with user in database
        // System.out.println(request.get);
        // List<User> users = new ArrayList<>();
        // this.userRepository.findAll().forEach(users::add);
        try {
            User user = this.userRepository.findByUsername(username);
            if (user != null && user.getPassword().trim().equals(password.trim())) {
                Cookie cookie = new Cookie("username", user.getUsername());
                response.addCookie(cookie);
                return "redirect:/";
            } else {
                return "redirect:/login?error=403";
            }
        } catch (Exception ex) {
            System.out.println("error: " + ex.getMessage());
            return "redirect:/login?error=500";
        }

    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        Cookie cookie = new Cookie("username", null);
        response.addCookie(cookie);
        cookie = new Cookie("JSESSIONID", null);
        // add cookie to response
        response.addCookie(cookie);

        return "redirect:/login";
    }

    @GetMapping("/donate")
    public ModelAndView donate(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView viewData = new ModelAndView("login");
        Cookie[] cookies = request.getCookies();
        if(!this.isAuthen(cookies)){
            viewData = new ModelAndView("redirect:/login");
            return viewData;
        }
        
        viewData.setViewName("donate");
        viewData.addObject("templeName", this.siteName);
        viewData.addObject("pageName", "donate");
        viewData.addObject("title", "ทำบุญ: ok");
        viewData.addObject("isValid", true);
        viewData.addObject("viewCert", false);
        return viewData;
    }

    @PostMapping("/donate")
    public ModelAndView okDonate(
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("name") String name,
            @RequestParam("donate_for") String donate_for,
            @RequestParam("amount") Double amount,
            @RequestParam("donate_date") String donate_date) {
        ModelAndView viewData = new ModelAndView("donate");
        Cookie[] cookies = request.getCookies();
        if(!this.isAuthen(cookies)){
            viewData = new ModelAndView("redirect:/login");
            return viewData;
        }

        // System.out.println(name + " : " + donate_for + " : " + amount);
        if(name.trim().equals("") || donate_for.trim().equals("") || amount == 0.0){
            viewData = new ModelAndView("donate");
            viewData.addObject("templeName", this.siteName);
            viewData.addObject("pageName", "donate");
            viewData.addObject("title", "ทำบุญ: ");
            viewData.addObject("isValid", false);
            viewData.addObject("viewCert", false);
            // System.out.println("invalid data found ........");
        }
        else{
            try{
                java.util.Date date = new java.util.Date();
                Timestamp timestamp = new Timestamp(date.getTime());
                DonationInfo donation = new DonationInfo();

                DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                // format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
                // convert donate_date from พศ to คศ
                String engYearDate = utilsService.convertThaiToEngYear(donate_date);
                java.util.Date tmpdate = format.parse(engYearDate);
                
                java.sql.Date sqlDate = new java.sql.Date(tmpdate.getTime());

                donation.setName(name);
                donation.setDONATE_FOR(donate_for);
                donation.setAMOUNT(amount);
                donation.setDonatedate(sqlDate);
                donation.setRECORD_DATE(timestamp);
                DonationInfo savedDonation = donationInfoRepository.save(donation);

                viewData.addObject("isValid", true);
                viewData.addObject("viewCert", true);
                viewData.addObject("donation", savedDonation);
                viewData.addObject("certLink", "http://localhost:8080/generatepdf/" + savedDonation.getID());
                // System.out.println("valid data and view cert......");
            }
            catch(Exception ex){
                System.out.println("Error....");
            }
        }
        viewData.addObject("templeName", this.siteName);
        viewData.addObject("pageName", "donate");
        viewData.addObject("title", "ทำบุญ: ");
        return viewData;

    }

    @GetMapping("/search")
    public ModelAndView search(
        HttpServletRequest request, 
        HttpServletResponse response,
        @RequestParam(required=false) Map<String,String> qparams)
            throws FileNotFoundException, JRException {
        Cookie[] cookies = request.getCookies();
        if(!this.isAuthen(cookies)){
            ModelAndView viewData = new ModelAndView("redirect:/login");
            return viewData;
        }
        String page = qparams.get("page");
        String name= qparams.get("name");
        String donate_date = qparams.get("donate_date");

        // System.out.println("donate_date: " + donate_date + ", name: " + name + ", page: " + page);

        ModelAndView viewData = new ModelAndView("search");
        viewData.addObject("templeName", this.siteName);
        viewData.addObject("pageName", "search");
        viewData.addObject("title", "ค้นหารายชื่อผู้บริจาค: ");

        // get data
        Pageable pagingParams = PageRequest.of(0, 20, Sort.by(Direction.DESC, "ID")); //PageRequest.of(0, 3);
        if(page != "" && page != null){
            Integer goToPage = Integer.parseInt(page);
            pagingParams = PageRequest.of(goToPage - 1, 20, Sort.by(Direction.DESC, "ID"));
        }

        
        if((name == "" || name == null) && (donate_date == "" || donate_date == null)){
            Page<DonationInfo> allProducts = donationInfoRepository.findAll(pagingParams);
            viewData.addObject("data", allProducts.getContent());
            viewData.addObject("TotalPage", allProducts.getTotalPages());
            viewData.addObject("pageSize", allProducts.getSize());
            viewData.addObject("currentPage", allProducts.getNumber());
            viewData.addObject("TotalItem", allProducts.getTotalElements());
            viewData.addObject("isValid", true);
        }
        else{
            if((name != "" || name != null) && (donate_date == "" || donate_date == null)){
                Page<DonationInfo> allProducts = donationInfoRepository.findByNameContaining(name, pagingParams); 
                viewData.addObject("data", allProducts.getContent());
                viewData.addObject("TotalPage", allProducts.getTotalPages());
                viewData.addObject("pageSize", allProducts.getSize());
                viewData.addObject("currentPage", allProducts.getNumber());
                viewData.addObject("TotalItem", allProducts.getTotalElements());
                viewData.addObject("isValid", true);
            }
            else if((name == "" || name == null) && (donate_date != "" || donate_date != null)){
                try{
                    DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                    String engYearDate = utilsService.convertThaiToEngYear(donate_date);
                    java.util.Date tmpdate = format.parse(engYearDate);
                    java.sql.Date queryDate = new java.sql.Date(tmpdate.getTime());

                    Page<DonationInfo> allProducts = donationInfoRepository.findByDonatedate(queryDate, pagingParams);
                    viewData.addObject("data", allProducts.getContent());
                    viewData.addObject("TotalPage", allProducts.getTotalPages());
                    viewData.addObject("pageSize", allProducts.getSize());
                    viewData.addObject("currentPage", allProducts.getNumber());
                    viewData.addObject("TotalItem", allProducts.getTotalElements());
                    viewData.addObject("isValid", true);
                }
                catch(Exception ex){
                    viewData.addObject("data", new ArrayList<DonationInfo>());
                    viewData.addObject("isValid", false);
                    viewData.addObject("currentPage", 1);
                }
            }
            else if((name != "" || name != null) && (donate_date != "" || donate_date != null)){
                try{
                    DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                    String engYearDate = utilsService.convertThaiToEngYear(donate_date);
                    java.util.Date tmpdate = format.parse(engYearDate);
                    java.sql.Date queryDate = new java.sql.Date(tmpdate.getTime());

                    Page<DonationInfo> allProducts = donationInfoRepository.findByNameContainingAndDonatedate(name , queryDate, pagingParams);
                    viewData.addObject("data", allProducts.getContent());
                    viewData.addObject("TotalPage", allProducts.getTotalPages());
                    viewData.addObject("pageSize", allProducts.getSize());
                    viewData.addObject("currentPage", allProducts.getNumber());
                    viewData.addObject("TotalItem", allProducts.getTotalElements());
                    viewData.addObject("isValid", true);
                }
                catch(Exception ex){
                    viewData.addObject("isValid", false);
                    viewData.addObject("data", new ArrayList<DonationInfo>());
                    viewData.addObject("currentPage", 1);
                }
            }
            
        }

        
        return viewData;
    }

    /// Generate PDF
    @PostMapping("/generatepdf")
    public ResponseEntity<byte[]> generateDonationCertificate(
            @RequestBody List<DonationInfo> data) throws JRException, FileNotFoundException {

        byte[] bytes = service.exportReport(data, "pdf");
        return ResponseEntity
                .ok()
                // Specify content type as PDF
                .header("Content-Type", "application/pdf; charset=UTF-8")
                // Tell browser to display PDF if it can
                .header("Content-Disposition", "inline; filename=\"certificate.pdf\"")
                .body(bytes);
    }

    @GetMapping("/generatepdf/{id}")
    public ResponseEntity<byte[]> generateDonationCertificate(@PathVariable("id") Long id)
            throws JRException, FileNotFoundException {
        List<DonationInfo> result = new ArrayList<>();
        Format formatter = new SimpleDateFormat("EEEE ที่ dd เดือน MMMM พ.ศ. yyyy", new Locale("th", "TH"));
        try {
            Optional<DonationInfo> donationInfoOptional = donationInfoRepository.findById(id);
            DonationInfo data = donationInfoOptional.get();
            // convert  คศ to พศ !!
            result.add(data);
            result.stream().forEach(donate -> {
                String a = new DecimalFormat("#,###.00").format(donate.getAMOUNT());
                donate.setAMOUNT_TH(utilsService.getText(a));
                String s = formatter.format(donate.getDonatedate());
                donate.setDonateDateTh(utilsService.getText(s));
            });
        } catch (Exception ex) {
            System.out.println(">>>> " + ex.getMessage());
        }

        // byte[] bytes = service.exportReport(result, "pdf");
        byte[] bytes = service.generatePDFReport(result);
        return ResponseEntity
                .ok()
                .header("Content-Type", "application/pdf; charset=UTF-8")
                .header("Content-Disposition", "inline; filename=\"certificate.pdf\"")
                .body(bytes);
    }
}
