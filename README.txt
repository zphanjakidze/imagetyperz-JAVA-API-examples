============================================================================================
Java API
============================================================================================
You can use the API by either compiling the source files or by importing the imagetypersAPI_lib.jar
file into your libraries. The package name is com.ImageTypers, and the class you're looking for is 
ImageTypersAPI.
============================================================================================
Below you have some examples on how to use the API library (taken from Program.java)
--------------------------------------------------------------------------------------------
package com.ImageTypers;

String username = "your_username_here";
String password = "your_password_here";

ImageTypersAPI i = new ImageTypersAPI(username, password);      // init API with your username and password

// get balance
// -------------------------
String balance = i.account_balance();
System.out.println(String.format("Balance: %s", balance));

// solve normal captcha
// -------------------------
System.out.println("Waiting for captcha to be solved ...");
String resp = i.solve_captcha("captcha.jpg", true);
System.out.println(String.format("com.ImageTypers.Captcha text: %s", resp));

// solve recaptcha
// -------------------------
// check: http://www.imagetyperz.com/Forms/recaptchaapi.aspx on how to get page_url and googlekey
String page_url = "your_page_url_here";
String sitekey = "your_sitekey_here";
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
======================================================================================================
[*] Requires Java installed