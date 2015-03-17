var Router = require("vertx-apex-js/router");
var SockJSHandler = require("vertx-apex-js/sock_js_handler");
var StaticHandler = require("vertx-apex-js/static_handler");

// Create an apex {@link Router}
var router = Router.router(vertx);

// Get the cached EventBus instance from Vertx.
var eb = vertx.eventBus();

// Register a listener on the {@link EventBus} to recieve messages from the client.
eb.consumer("chat.to.server").handler(function (message) {
  // When a message is recieved, prepend a timestamp and send the message back to all clients.
  var now = Java.type("java.util.Date").from(Java.type("java.time.Instant").now());
  var timestamp = Java.type("java.text.DateFormat").getDateTimeInstance(Java.type("java.text.DateFormat").SHORT, Java.type("java.text.DateFormat").MEDIUM).format(now);
  eb.publish("chat.to.client", timestamp + ": " + message.body());
});

// Configure the {@link EventBus} bridge allowing only the specified addresses in/out.
var opts = {
  "inboundPermitteds" : [
    {
      "address" : "chat.to.server"
    }
  ],
  "outboundPermitteds" : [
    {
      "address" : "chat.to.client"
    }
  ]
};

var ebHandler = SockJSHandler.create(vertx).bridge(opts);
router.route("/eventbus/*").handler(ebHandler.handle);

router.route().handler(StaticHandler.create("webroot/").setIndexPage("chat.html").handle);

vertx.createHttpServer().requestHandler(router.accept).listen(8000);
