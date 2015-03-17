package io.vertx.example.apex.realtime;

import io.vertx.codetrans.annotations.CodeTranslate;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Verticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.handler.StaticHandler;
import io.vertx.ext.apex.handler.sockjs.BridgeOptions;
import io.vertx.ext.apex.handler.sockjs.PermittedOptions;
import io.vertx.ext.apex.handler.sockjs.SockJSHandler;
import static java.text.DateFormat.MEDIUM;
import static java.text.DateFormat.SHORT;
import static java.text.DateFormat.getDateTimeInstance;
import static java.time.Instant.now;
import java.util.Date;

/**
 * A simple {@link Verticle} showing how to use the EventBus bridge for SockJS websocket communication.
 * @author <a href="https://github.com/InfoSec812">Deven Phillips</a>
 */

public class ChatVerticle extends AbstractVerticle {
    
    @CodeTranslate
    @Override
    public void start() throws Exception {

        // Create an apex {@link Router}
        Router router = Router.router(vertx);
        
        // Get the cached EventBus instance from Vertx.
        final EventBus eb = vertx.eventBus();
        
        // Register a listener on the {@link EventBus} to recieve messages from the client.
        eb.consumer("chat.to.server").handler(message -> {
            // When a message is recieved, prepend a timestamp and send the message back to all clients.
            String timestamp = getDateTimeInstance(SHORT, MEDIUM).format(Date.from(now()));
            eb.publish("chat.to.client", timestamp+": "+message.body());
        });
        
        // Configure the {@link EventBus} bridge allowing only the specified addresses in/out.
        BridgeOptions opts = new BridgeOptions()
                                    .addInboundPermitted(new PermittedOptions().setAddress("chat.to.server"))
                                    .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client"));
        
        SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
        router.route("/eventbus/*").handler(ebHandler);
        
        router.route().handler(StaticHandler.create("webroot/").setIndexPage("chat.html"));
        
        vertx.createHttpServer().requestHandler(router::accept).listen(8000);
    }
}
