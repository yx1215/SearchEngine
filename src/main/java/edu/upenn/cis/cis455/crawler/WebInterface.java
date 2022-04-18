package edu.upenn.cis.cis455.crawler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.*;
import spark.Session;

import edu.upenn.cis.cis455.crawler.handlers.*;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.documentStorage.StorageInterface;
import org.apache.logging.log4j.Level;


public class WebInterface {

    public static void main(String args[]) {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
        if (args.length < 1 || args.length > 2) {
            System.out.println("Syntax: WebInterface {path} {root}");
            System.exit(1);
        }

        if (!Files.exists(Paths.get(args[0]))) {
            try {
                Files.createDirectory(Paths.get(args[0]));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        port(45555);
        StorageInterface database = StorageFactory.getDocumentDatabase(args[0]);
        database.addAdmin();
        LoginFilter testIfLoggedIn = new LoginFilter(database);

        if (args.length == 2) {
//            staticFiles.externalLocation(args[1]);
            staticFileLocation(args[1]);
        }


//        before("/*", "*/*", testIfLoggedIn);
        before(testIfLoggedIn);
        // TODO:  add /register, /logout, /index.html, /, /lookup
        //post("/register", new RegistrationHandler(database));
        get("/login", (req, res) -> {
            res.redirect("/login-form.html");
            return "";
        });

        post("/login", new LoginHandler(database));
        post("/register", new RegisterHandler(database));
        get("/logout", (req, res) -> {
            Session session = req.session(false);
            if (session != null){
                session.invalidate();
            }
            res.redirect("/login-form.html");
            return "";
        });

        get("/", new HomePageHandler(database));

        get("/index.html", new HomePageHandler(database));

        get("/lookup", new LookupHandler(database));

        get("/show", new ShowHandler(database));

        get("/create/:name", new CreateChannelHandler(database));

        get("/subscribe", new SubscriptionHandler(database));

        post("/startCrawler", new StartCrawlerHandler(database));

        awaitInitialization();
    }
}
