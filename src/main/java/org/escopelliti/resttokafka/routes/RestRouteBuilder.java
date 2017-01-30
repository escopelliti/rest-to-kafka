package org.escopelliti.resttokafka.routes;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.model.rest.RestBindingMode;

public class RestRouteBuilder extends RouteBuilder {

    final private Config config;

    public RestRouteBuilder() {
        super();
        this.config = ConfigFactory.load();
    }

    @Override
    public void configure() throws Exception {

        String route = config.getString("rest.route");
        Integer port = config.getInt("infrastructure.port");
        String host = config.getString("infrastructure.host");
        final String contextRoot = config.getString("rest.context_root");
        String toComponent = config.getString("to");
        String toEndpoint = config.getString("infrastructure." + toComponent + ".endpoint");

        log.info("Consuming from {}. Routing to {}", host + ":" + port + "/" + contextRoot + "/" + route, toEndpoint);
        restConfiguration()
                .component("netty4-http")
                .host(host)
                .port(port)
                .bindingMode(RestBindingMode.auto)
                .enableCORS(true);

        rest("/" + contextRoot)
                .post("/" + route)
                .consumes("application/json")
                .to("direct:" + route);

        final String routeId = contextRoot + "/" + route;
        from("direct:" + route)
                .routeId(routeId)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        String body = exchange.getIn().getBody(String.class);
                        exchange.getIn().setBody(body.replaceAll("\n", ""), String.class);
                        exchange.getIn().setHeader(KafkaConstants.KEY, routeId);
                    }
                })
                .log(LoggingLevel.DEBUG, "${routeId}")
                .to(toEndpoint);
    }
}
