package com.ImageTypers;

import java.util.concurrent.TimeUnit;

/**
 * Created by icebox on 22/05/17.
 */
public class Program {
    // test_api API method
    private static void test_api() throws Exception {
        String username = "user name here";
        String password = "password here";

        ImageTypersAPI i = new ImageTypersAPI(username, password);      // init API with your username and password

        // get balance
        // -------------------------
        String balance = i.account_balance();
        System.out.println(String.format("Balance: %s", balance));

        // solve normal captcha
        // -------------------------
        System.out.println("Waiting for captcha to be solved ...");
        String resp = i.solve_captcha("captcha.jpg", true);
        System.out.println(String.format("Captcha text: %s", resp));

        // solve recaptcha
        // -------------------------
        // check: http://www.imagetyperz.com/Forms/recaptchaapi.aspx on how to get page_url and googlekey
        String page_url = "page url here";
        String sitekey = "key code here";
        // submit captcha first to get ID
        String captcha_id = i.submit_recaptcha(page_url, sitekey);
        System.out.println("Waiting for recaptcha to be solved ...");
        while(i.in_progress(captcha_id))
        {
            TimeUnit.SECONDS.sleep(10);     // sleep for 10 seconds
        }
        // completed at this point
        String recaptcha_response = i.retrieve_captcha(captcha_id);     // get recaptcha response
        System.out.println(String.format("com.ImageTypers.Recaptcha response: %s", recaptcha_response));

        // Other examples
        // ----------------------
        // com.ImageTypers.ImageTypersAPI i = new com.ImageTypers.ImageTypersAPI("testingfor", "testingfor", "5");      // example with ref_id
        // i.submit_recaptcha(page_url, sitekey, "127.0.0.1:1234", "HTTP");             // solve recaptcha with proxy
        // System.out.println(i.set_captcha_bad(i.captcha_id()));                       // set captcha bad

        // System.out.println(i.captcha_id());                                          // last captcha solved id
        // System.out.println(i.captcha_text());                                        // last captcha solved text
        // System.out.println(i.recaptcha_id());                                        // last recaptcha solved id
        // System.out.println(i.recaptcha_response());                                  // last recaptcha solved response
        // System.out.println(i.error());                                               // last error
    }

    // command-line mode
    private static void command_line(String[] args) throws Exception {
        new com.ImageTypers.CommandLineController(args).run();      // run commandline controller
    }

    // main/run method
    public static void main(String[] args) {
        try
        {
            com.ImageTypers.Program.test_api();        // test_api API
            //com.ImageTypers.Program.command_line(args);      // commandline
        }
        catch(Exception ex)
        {
            System.out.println(String.format("Error occured: %s", ex.getMessage()));     // print exception message
        }
    }
}