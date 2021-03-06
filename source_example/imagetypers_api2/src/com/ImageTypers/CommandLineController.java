package com.ImageTypers;

import org.apache.commons.cli.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by icebox on 25/05/17.
 */
public class CommandLineController {
    private CommandLine _commandLine;
    private Arguments _args;

    // constructor
    public CommandLineController(String[] args) throws Exception {
        this.init_arguments(args);         // init parser
        this.parse_arguments();            // arguments were initialized, parse them
    }

    // Init arguments
    private void init_arguments(String[] args) throws ParseException {
        Option option_u = Option.builder("u")
                .required(true)
                .desc("Account username")
                .hasArg()
                .build();
        Option option_p = Option.builder("p")
                .required(true)
                .desc("Account password")
                .hasArg()
                .build();

        Option option_m = Option.builder("m")
                .required(true)
                .desc("Mode (1-5)")
                .hasArg()
                .build();

        Option option_i = Option.builder("i")
                .required(false)
                .desc("com.ImageTypers.Captcha image file path")
                .hasArg()
                .build();
        Option option_o = Option.builder("o")
                .required(false)
                .desc("Output file")
                .hasArg()
                .build();

        Option option_refid = Option.builder("refid")
                .required(false)
                .desc("Reference/affiliate ID")
                .hasArg()
                .build();
        Option option_page = Option.builder("pageurl")
                .required(false)
                .desc("Page URL (requires recaptcha mode 2)")
                .hasArg()
                .build();
        Option option_key = Option.builder("sitekey")
                .required(false)
                .desc("Site key (requires recaptcha mode 2)")
                .hasArg()
                .build();
        Option option_id = Option.builder("captchaid")
                .required(false)
                .desc("com.ImageTypers.Captcha ID")
                .hasArg()
                .build();
        Option option_case = Option.builder("case")
                .required(false)
                .desc("Case sensitivity (1 or 0)")
                .hasArg()
                .build();
        Option option_proxy = Option.builder("proxy")
                .required(false)
                .desc("Proxy in format IP:Port")
                .hasArg()
                .build();
        Option option_proxytype = Option.builder("proxytype")
                .required(false)
                .desc("Proxy type")
                .hasArg()
                .build();

        Options options = new Options();
        options.addOption(option_u);
        options.addOption(option_p);
        options.addOption(option_m);
        options.addOption(option_i);
        options.addOption(option_o);
        options.addOption(option_refid);
        options.addOption(option_page);
        options.addOption(option_key);
        options.addOption(option_id);
        options.addOption(option_case);
        options.addOption(option_proxy);
        options.addOption(option_proxytype);
        CommandLineParser parser = new DefaultParser();

        try{
            this._commandLine = parser.parse(options, args);      // set commandline
        }
        catch(Exception ex)
        {
            this.save_error(String.format("Cannot init arguments, details: " + ex.getMessage()));        // save error and re-throw
            throw ex;
        }
    }

    // Parse arguments
    private void parse_arguments() {
        CommandLine c = this._commandLine;      // for easier use
        this._args = new Arguments();       // init args object

        // set the user, pass & mode which are required
        String u = c.getOptionValue("u");
        this._args.set_username(u);
        this._args.set_password(c.getOptionValue("p"));
        this._args.set_mode(c.getOptionValue("m"));

        // init the optional ones
        if (c.hasOption("i"))
        {
            this._args.set_captcha_file((c.getOptionValue("i")));
        }
        if(c.hasOption("o"))
        {
            this._args.set_output_file((c.getOptionValue("o")));
        }
        if(c.hasOption("pageurl"))
        {
            this._args.set_page_url((c.getOptionValue("pageurl")));
        }
        if(c.hasOption("sitekey"))
        {
            this._args.set_site_key((c.getOptionValue("sitekey")));
        }
        if(c.hasOption("captchaid"))
        {
            this._args.set_captcha_id((c.getOptionValue("captchaid")));
        }
        if(c.hasOption("case"))
        {
            boolean b = false;
            String cc = c.getOptionValue("case");
            if(cc.equals("1"))
                b = true;       // make it true
            this._args.set_case_sensitive(b);
        }
        if(c.hasOption("proxy"))
        {
            this._args.set_proxy(c.getOptionValue("proxy"));
        }
        if(c.hasOption("proxytype"))
        {
            this._args.set_proxy_type(c.getOptionValue("proxytype"));
        }
    }

    // run method
    public void run() throws Exception {
        try {
            this._run();        // run private
        }
        catch(Exception ex)
        {
            this.save_error(ex.getMessage());       // save to file
            throw ex;       // rethrow
        }
    }

    private void _run() throws Exception {
        ImageTypersAPI i = new ImageTypersAPI(this._args.get_username(), this._args.get_password());      // init API with your username and password

        switch(this._args.get_mode())
        {
            case "1":
                // solve normal captcha
                // -------------------------
                String captcha_file = this._args.get_captcha_file();
                if(captcha_file.isEmpty())
                    throw new Exception("Invalid captcha file");
                boolean cs = this._args.is_case_sensitive();
                String resp = i.solve_captcha(captcha_file, cs);
                this.show_output(resp);
                break;
            case "2":
                String page_url = this._args.get_page_url();
                if (page_url.isEmpty())
                    throw new Exception("Invalid recaptcha pageurl");
                String site_key = this._args.get_site_key();
                if (site_key.isEmpty())
                    throw new Exception("Invalid recaptcha sitekey");
                String proxy = this._args.get_proxy();
                String proxy_type = this._args.get_proxy_type();

                String captcha_id = "";

                // check proxy
                if(!proxy.isEmpty() && !proxy_type.isEmpty())
                {
                    captcha_id = i.submit_recaptcha(page_url, site_key, proxy, proxy_type);
                }
                else
                {
                    captcha_id = i.submit_recaptcha(page_url, site_key);
                }
                this.show_output(captcha_id);
                break;
            case "3":
                String recaptcha_id = this._args.get_captcha_id();
                if(recaptcha_id.isEmpty())
                    throw new Exception("recaptcha captchaid is invalid");
                String recaptcha_response = i.retrieve_captcha(recaptcha_id);     // get recaptcha response
                this.show_output(recaptcha_response);       // show response
                break;
            case "4":
                // get balance
                // -------------------------
                String balance = i.account_balance();
                this.show_output(balance);      // show balance
                break;
            case "5":
                String bad_id = this._args.get_captcha_id();
                if(bad_id.isEmpty())
                    throw new Exception("captchaid is invalid");
                String response = i.set_captcha_bad(bad_id);        // set it bad
                this.show_output(response);     // show response
                break;
        }
    }

    // Save error to text file
    private void save_error(String error)
    {
        this.save_text("error.txt", error);
    }
    // Show output (screen & outputfile [if given])
    private void show_output(String output)
    {
        System.out.println(output);     // print on screen first

        // save to file
        String o_file = this._args.get_output_file();
        if(o_file.isEmpty())
            return;             // return if we don't have an output file

        this.save_text(o_file, output);
    }

    // Save text to file
    private void save_text(String filename, String text)
    {
        PrintWriter out = null;
        try {
            out = new PrintWriter(filename); // open file
            out.print(text.trim());     // save text
        } catch (FileNotFoundException e) {
            System.out.println(String.format("Error occured while saving text to file, details: %s", e.getMessage()));
        }
        finally
        {
            if(out != null)
            {
                out.close();        // close it safe
            }
        }
    }
}
