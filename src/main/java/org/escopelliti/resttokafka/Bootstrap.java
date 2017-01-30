package org.escopelliti.resttokafka;


import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;
import org.escopelliti.resttokafka.routes.RestRouteBuilder;

@Slf4j
public class Bootstrap {

    public static void main(String[] args) {

        Main main = new Main();
        main.addMainListener(new MainListenerSupport() {
            public void configure(CamelContext context) {
                try {
                    context.addRoutes(new RestRouteBuilder());
                } catch (Exception e) {
                    log.error("Error while creating / adding routes. ", e.toString());
                }

            }
        });

        try {
            main.run();
        } catch (Exception e) {
            log.error("General error thrown. Exiting. ", e.toString());
        }
    }
}
