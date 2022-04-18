package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;
import static spark.Spark.halt;


import edu.upenn.cis.cis455.storage.documentStorage.StorageInterface;


public class RegisterHandler implements Route {
    StorageInterface db;

    public RegisterHandler(StorageInterface db) {
        this.db = db;
    }

    @Override
    public String handle(Request req, Response res) throws HaltException {
        String user = req.queryParams("username");
        String pass = req.queryParams("password");

        System.err.println("Registering for " + user + " and " + pass);
        if (db.getPwd(user) != null){
            halt(409, String.format("<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>Register account</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h1>Create Account for Milestone 2</h1>\n" +
                    "<p>Username %s exists. Please try another one.</p>\n" +
                    "</br>" +
                    "<a href=\"register.html\">Register Again</a>" +
                    "</body>\n" +
                    "</html>", user));
        }
        db.addUser(user, pass);
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Register account</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1>Create Account for Milestone 2</h1>\n" +
                "<p>Account Created Successfully.</p>\n" +
                "<a href=\"login-form.html\">Login</a>" +
                "</body>\n" +
                "</html>";

    }
}